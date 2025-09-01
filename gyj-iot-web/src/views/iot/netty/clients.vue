<template>
    <div class="app-container">
        <el-form @submit.native.prevent :model="queryParams" ref="queryRef" :inline="true">
            <el-form-item label="客户端" prop="clientId">
                <el-input v-model="queryParams.clientId" placeholder="请输入客户端ID" clearable @keyup.enter.native="handleQuery" />
            </el-form-item>
            <el-form-item prop="isClient">
                <el-checkbox v-model="queryParams.isClient" true-label="1" false-label="0">只看设备端</el-checkbox>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
        </el-form>

        <el-table v-loading="loading" :data="clientList" border>
            <el-table-column label="客户端ID" align="left" header-align="center" prop="clientId">
                <template #default="scope">
                    <el-link :underline="false" type="primary" @click.native="handleOpen(scope.row)">{{ scope.row.clientId }}</el-link>
                </template>
            </el-table-column>
            <el-table-column label="类型" align="center" prop="type">
                <template #default="scope">
                    <el-tag type="danger" v-if="scope.row.clientId.indexOf('server') == 0">服务端</el-tag>
                    <el-tag type="success" v-else-if="scope.row.clientId.indexOf('web') == 0">Web端</el-tag>
                    <el-tag type="warning" v-else-if="scope.row.clientId.indexOf('phone') == 0">移动端</el-tag>
                    <el-tag type="info" v-else-if="scope.row.clientId.indexOf('test') == 0">测试端</el-tag>
                    <el-tag type="primary" v-else>设备端</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="连接状态" align="center" prop="connected">
                <template #default="scope">
                    <el-tag type="success" v-if="scope.row.connected">已连接</el-tag>
                    <el-tag type="info" v-else>已断开</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="心跳(秒)" align="center" prop="keepAlive" width="100" />
            <el-table-column label="账号" align="center" prop="username" width="100px" />
            <el-table-column label="当前订阅数量" align="center" prop="topicCount" width="120" />
            <el-table-column label="连接时间" align="center" prop="connected_at" />

            <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button type="primary" icon="Edit" @click="clickClientOut(scope.row)">踢出</el-button>
                </template>
            </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
                    v-model:limit="queryParams.pageSize" @pagination="getList" />

        <!-- MQTT客户端详细 -->
        <el-dialog :title="title" v-model="open" width="800px" append-to-body>
            <el-tabs v-model="activeName" tab-position="top" style="padding: 10px">
                <el-tab-pane label="订阅列表" name="subscribe">
                    <el-row :gutter="10" class="mb8"></el-row>
                    <el-table :data="subscribeList" size="mini">
                        <el-table-column label="主题" align="center" prop="topicName" />
                        <el-table-column label="QoS" align="center" prop="qos" />
                    </el-table>
                </el-tab-pane>
            </el-tabs>
        </el-dialog>

        <!-- 添加或修改订阅对话框 -->
        <el-dialog title="添加订阅" v-model="subscribeOpen" width="800px" append-to-body>
            <el-form ref="subscribeForm" :model="subscribeForm" :rules="rules" label-width="60px">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="主题" prop="topic">
                            <el-input v-model="subscribeForm.topic" placeholder="请输入主题" />
                        </el-form-item>
                        <el-form-item label="Qos" prop="qos">
                            <el-select v-model="subscribeForm.qos" placeholder="请选择消息类型">
                                <el-option key="0" label="0" value="0"></el-option>
                                <el-option key="1" label="1" value="1"></el-option>
                                <el-option key="2" label="2" value="2"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button type="primary" @click="submitForm">添 加 订 阅</el-button>
                    <el-button @click="cancelSubscribe">取 消</el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script setup name="Clients">
import { clientOut, listNettyMqttClient } from '@/api/iot/netty';

const { proxy } = getCurrentInstance();

// 非单个禁用
const single = ref(true);
// 遮罩层
const loading = ref(true);
const loadSubscribeing = ref(true);
// 显示搜索条件
const showSearch = ref(true);
// 总条数
const total = ref(0);
// 产品分类表格数据
const clientList = ref([]);
// 弹出层标题
const title = ref('');
// 是否显示弹出层
const open = ref(false);
// 是否显示添加订阅弹出层
const subscribeOpen = ref(false);
// 选中选项卡
const activeName = ref('subscribe');
//订阅列表数据
const subscribeList = ref([]);
//订阅数据
const subscribe = ref({
    topic: '',
    clientId: '',
});
//添加订阅表单参数
const subscribeForm = ref({
    qos: '0',
});
//客户端ID
const clientId = ref('');
//服务协议类型
const serverType = ref('MQTT');

const data = reactive({
    // 表单参数
    form: {},
    // 查询参数
    queryParams: {
        pageNum: 1,
        pageSize: 10,
        clientId: null,
        isClient: 0,
        serverCode: 'MQTT',
    },
    // 表单校验
    rules: {
        topic: [
            {
                required: true,
                message: '主题不能为空',
                trigger: 'blur',
            },
        ],
    },
})
const { queryParams, form, rules } = toRefs(data);

/** 查询客户端列表 */
function getList() {
    loading.value = true;
    listNettyMqttClient(queryParams.value).then((response) => {
        clientList.value = response.data;
        total.value = response.total;
        loading.value = false;
    });
}
/*将客户端踢出*/
function clickClientOut(row) {
    const params = { clientId: row.clientId };
    clientOut(params).then((res) => {
        //刷新列表
        getList();
    });
}
/*tabs切换*/
function handleClick() {
    queryParams.value.serverCode = serverType.value;
    getList();
}
/** 查询客户端订阅列表 */
function getSubscribeList(clientId) {
    clientId.value = clientId;
    loadSubscribeing.value = true;
    getSubscriptionsByClientId(clientId).then((res) => {
        subscribeList.value = res.data.data;
        loadSubscribeing.value = false;
    });
}
// 取消按钮
function cancel() {
    open.value = false;
    reset();
}
/** 搜索按钮操作 */
function handleQuery() {
    queryParams.value.pageNum = 1;
    getList();
}
/** 重置按钮操作 */
function resetQuery() {
    proxy.resetForm('queryForm');
    handleQuery();
}
/** 断开客户端连接 */
function handleDelete(row) {
    const clientId = row.clientId;
    proxy.$modal.confirm('是否确认删除MQTT客户端编号为"' + clientId + '"的数据项？')
        .then(function () {
            return eliminateClient(clientId);
        })
        .then(() => {
            getList();
            proxy.$modal.msgSuccess('删除成功');
        })
        .catch(() => {});
}
//取消主题订阅
function handleUnsubscribe(row) {
    const clientId = row.clientId;
    const topic = row.topic;
    proxy.$modal.confirm('是否确认取消订阅主题为"' + topic + '"的数据项？')
        .then(function () {
            const param = {};
            param.topic = topic;
            param.clientId = clientId;
            return unsubscribe(param);
        })
        .then(() => {
            getSubscribeList(clientId);
            proxy.$modal.msgSuccess('取消订阅成功');
        })
        .catch(() => {});
}
//查看客户端详情
function handleOpen(row) {
    open.value = true;
    title.value = '详情';
    subscribeList.value = row.topics;
}
//添加订阅
function handleAdd() {
    subscribeOpen.value = true;
}
//提交添加订阅表单
function submitForm() {
    subscribeForm.value.clientId = clientId.value;
    proxy.$refs['subscribeForm'].validate((valid) => {
        if (valid) {
            addSubscribe(subscribeForm.value).then((response) => {
                proxy.$modal.msgSuccess('新增订阅成功');
                subscribeOpen.value = false;
                getSubscribeList(clientId.value);
            });
        }
    });
}
function cancelSubscribe() {
    subscribeOpen.value = false;
    proxy.resetForm('subscribeForm');
    //刷新列表
    getSubscribeList(clientId.value);
}

getList();
</script>
