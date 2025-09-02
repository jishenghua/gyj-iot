<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="60px">
            <el-form-item label="设备ID" prop="deviceSipId">
                <el-input v-model="queryParams.deviceSipId" placeholder="请输入设备编号" clearable @keyup.enter.native="handleQuery" />
            </el-form-item>
            <el-form-item label="通道ID" prop="channelSipId">
                <el-input v-model="queryParams.channelSipId" placeholder="请输入通道ID" clearable @keyup.enter.native="handleQuery" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
                <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
                    <el-option v-for="dict in sip_gen_status" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
            <el-form-item style="float: left">
                <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['iot:video:add']" :disabled="isGeneralUser">批量生成</el-button>
                <el-button type="danger" plain icon="Delete" :disabled="multiple || isGeneralUser" @click="handleDelete" v-hasPermi="['iot:video:remove']">批量删除</el-button>
            </el-form-item>
        </el-form>

        <el-table v-loading="loading" :data="sipidList" @selection-change="handleSelectionChange" @cell-dblclick="celldblclick" size="" border>
            <el-table-column type="selection" :selectable="selectable" width="55" align="center" />
            <el-table-column label="设备编号" align="center" prop="deviceSipId">
                <template #default="scope">
                    <el-link :underline="false" type="primary" @click="handleViewDevice(scope.row.deviceSipId)">{{ scope.row.deviceSipId }}</el-link>
                </template>
            </el-table-column>
            <el-table-column label="通道ID" align="center" prop="channelSipId" />
            <el-table-column label="状态" align="center" prop="status" width="80">
                <template #default="scope">
                    <dict-tag :options="sip_gen_status" :value="scope.row.status" />
                </template>
            </el-table-column>
            <el-table-column label="所属产品" align="center" prop="productName" />
            <el-table-column label="设备类型" align="center" prop="deviceType">
                <template #default="scope">
                    <dict-tag :options="video_type" :value="scope.row.deviceType" />
                </template>
            </el-table-column>
            <el-table-column label="通道类型" align="center" prop="channelType">
                <template #default="scope">
                    <dict-tag :options="channel_type" :value="scope.row.channelType" />
                </template>
            </el-table-column>
            <el-table-column label="行政区域" align="center" prop="citycode" />
            <el-table-column label="注册时间" align="center" prop="registerTime" width="180">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.registerTime, '{y}-{m}-{d} {h}:{m}:{s}') }}</span>
                </template>
            </el-table-column>
            <!-- <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template slot-scope="scope">
                <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row,'remark')" v-hasPermi="['iot:video:edit']">修改
                </el-button>
                <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['iot:video:remove']" v-if="!scope.row.deviceSipId">删除
                </el-button>
            </template>
        </el-table-column> -->
        </el-table>
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

        <el-dialog :title="title" v-model="open" width="450px" append-to-body :close-on-click-modal="false">
            <el-form :model="createForm" :rules="createFormrules" label-width="80px" ref="createFormRef">
                <el-form-item label="行政区划" prop="city">
                    <el-cascader :options="cityOptions" v-model="createForm.city" @change="changeProvince" change-on-select style="width: 330px" :props="{ checkStrictly: false }"></el-cascader>
                </el-form-item>
                <el-form-item label="设备类型" prop="deviceType">
                    <el-select v-model="createForm.deviceType" placeholder="请选择设备类型" style="width: 330px">
                        <el-option v-for="dict in video_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="通道类型" prop="channelType">
                    <el-select v-model="createForm.channelType" placeholder="请选择设备类型" style="width: 330px">
                        <el-option v-for="dict in channel_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="所属产品" prop="productName">
                    <el-input readonly v-model="createForm.productName" placeholder="请选择产品">
                        <template #append>
                            <el-button @click="selectProduct()" size="small">选择</el-button>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item label="通道数量" prop="createNum">
                    <el-input-number controls-position="right" v-model="createForm.createNum" :min="1" :max="10" placeholder="请输入生成通道数量" type="number" style="width: 330px" />
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button type="primary" @click="submitForm">生 成</el-button>
                    <el-button @click="cancel">取 消</el-button>
                </div>
            </template>
        </el-dialog>

        <!-- 选择产品 -->
        <product-list ref="productListRef" @productEvent="getProductData($event)" />
    </div>
</template>

<style>
.createNum {
    width: 300px;
}

.createNum input {
    width: 260px;
}
</style>

<script setup name="Sip">
import { getDeviceBySerialNumber } from '@/api/iot/device';
import { regionData, CodeToText } from 'element-china-area-data';
import { listChannel, getChannel, delChannel, addChannel } from '@/api/iot/channel';
import productList from './product-list.vue';
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()

const { proxy } = getCurrentInstance();
const { sip_gen_status, video_type, channel_type } = proxy.useDict('sip_gen_status', 'video_type', 'channel_type');

// 是否普通用户
const isGeneralUser = ref(true);
// 遮罩层
const loading = ref(true);
// 选中数组
const ids = ref([]);
// 非多个禁用
const multiple = ref(true);
// 总条数
const total = ref(0);
// sipid表格数据
const sipidList = ref([]);
// 弹出层标题
const title = ref('');
// 是否显示弹出层
const open = ref(false);
// 产品
const productInfo = ref({});
// 城市
const cityOptions = ref(regionData);
const city = ref('');
// 表单校验
//createForm表单校验
const createFormrules = ref({
    city: [{ required: true, message: '行政区划不能为空', trigger: 'change' }],
    deviceType: [{ required: true, message: '设备类型不饿能为空', trigger: 'change' }],
    channelType: [{ required: true, message: '通道类型不能为空', trigger: 'change' }],
    productName: [{ required: true, message: '产品类型不能为空', trigger: 'blur' }],
    createNum: [{ required: true, message: '请输入生成通道数量', trigger: 'change' }],
});

const data = reactive({
    form: {},
    // 查询参数
    queryParams: {
        pageNum: 1,
        pageSize: 10,
        deviceSipId: null,
        deviceChannelId: null,
        status: null,
    },
    createForm: {
        city: '',
        deviceType: '',
        channelType: '',
        createNum: 1,
        remark: '',
        area: '',
    },
    rules: {
        protocol: [
            {
                required: true,
                message: '默认播放协议不能为空',
                trigger: 'blur',
            },
        ],
        ip: [
            {
                required: true,
                message: '服务器ip不能为空',
                trigger: 'blur',
            },
        ],
        domain: [
            {
                required: true,
                message: '服务器域名不能为空',
                trigger: 'blur',
            },
        ],
        secret: [
            {
                required: true,
                message: '流媒体密钥不能为空',
                trigger: 'blur',
            },
        ],
        portHttp: [
            {
                required: true,
                message: 'http端口不能为空',
                trigger: 'blur',
            },
        ],
        portHttps: [
            {
                required: true,
                message: 'https端口不能为空',
                trigger: 'blur',
            },
        ],
        portRtmp: [
            {
                required: true,
                message: 'rtmp端口不能为空',
                trigger: 'blur',
            },
        ],
        portRtsp: [
            {
                required: true,
                message: 'rtsp端口不能为空',
                trigger: 'blur',
            },
        ],
        rtpPortRange: [
            {
                required: true,
                message: 'rtp端口范围不能为空',
                trigger: 'blur',
            },
        ],
        delFlag: [
            {
                required: true,
                message: '删除标志不能为空',
                trigger: 'blur',
            },
        ],
        createBy: [
            {
                required: true,
                message: '创建者不能为空',
                trigger: 'blur',
            },
        ],
        createTime: [
            {
                required: true,
                message: '创建时间不能为空',
                trigger: 'blur',
            },
        ],
    }
})
const { queryParams, form, rules, createForm } = toRefs(data);

const props = defineProps({
    product: {
        type: Object,
        default: null,
    },
});

// watch(
//     () => props.product,  // 监听源
//     (newVal, oldVal) => {
//         productInfo.value = newVal;
//         if (productInfo.value && productInfo.value.productId != 0) {
//             queryParams.value.productId = productInfo.value.productId;
//             deviceParams.value.productId = productInfo.value.productId;
//             getList();
//         }
//     }
// );

function init() {
    // 普通用户只能查看自己的通道
    if (userStore.roles.indexOf('general') === -1) {
        isGeneralUser.value = false
    }
}
/** 查看设备操作 */
function handleViewDevice(serialNumber) {
    proxy.$router.push({
        path: '/iot/device',
        query: {
            t: Date.now(),
            sn: serialNumber,
        },
    });
}
/**选择产品 */
function selectProduct() {
    proxy.$refs['productListRef'].show(form.value.productId);
}
/**获取选中的产品 */
function getProductData(data) {
    console.log(data)
    createForm.value.productId = data.productId;
    createForm.value.productName = data.productName;
    createForm.value.tenantId = data.tenantId;
    createForm.value.tenantName = data.tenantName;
}
/** 行政区划改变 **/
function changeProvince(data) {
    if (data && data[0] != null && data[1] != null && data[2] != null) {
        const str = CodeToText[data[0]] + '/' + CodeToText[data[1]] + '/' + CodeToText[data[2]];
        createForm.value.citycode = str;
    }
}
/**获取设备详情*/
function searchDeviceBySerialNumber(serialNumber) {
    openDevice.value = true;
    getDeviceBySerialNumber(serialNumber).then((response) => {
        device.value = response.data;
    });
}
/** 查询通道列表 */
function getList() {
    listChannel(queryParams.value).then((response) => {
        sipidList.value = response.rows;
        total.value = response.total;
        loading.value = false;
    });
}
// 取消按钮
function cancel() {
    open.value = false;
    reset();
}
// 表单重置
function reset() {
    createForm.value = {
        id: null,
        deviceSipId: null,
        channelSipId: null,
        status: 0,
        registertime: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null,
        createNum: 1,
    };
    proxy.resetForm('createFormRef');
}
/** 搜索按钮操作 */
function handleQuery() {
    loading.value = true;
    queryParams.value.pageNum = 1;
    getList();
}
/** 重置按钮操作 */
function resetQuery() {
    proxy.resetForm('queryRef');
    handleQuery();
}
// 多选框选中数据
function handleSelectionChange(selection) {
    ids.value = selection.map((item) => item.id);
    multiple.value = !selection.length;
}
/** 批量新增按钮操作 */
function handleAdd() {
    reset();
    open.value = true;
    title.value = '生成设备通道';
}
/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const id = row.id || ids.value;
    console.log(row);
    getChannel(id).then((response) => {
        createForm.value = response.data;
        open.value = true;
        title.value = '修改产品分类';
    });
}
/** 提交按钮 */
function submitForm() {
    proxy.$refs['createFormRef'].validate((valid) => {
        if (valid) {
            createForm.value.deviceSipId = createForm.value.city[2] + '0000' + createForm.value.deviceType + '0';
            createForm.value.channelSipId = createForm.value.city[2] + '0000' + createForm.value.channelType + '0';
            addChannel(createForm.value.createNum, createForm.value).then((response) => {
                proxy.$modal.msgSuccess('新增成功');
                open.value = false;
                getList();
            });
        }
    });
}
/** 删除按钮操作 */
function handleDelete(row) {
    const sipIds = row.id || ids.value;
    proxy.$modal.confirm('是否确认删除ID为"' + sipIds + '"的数据项？').then(function () {
            return delChannel(sipIds);
        })
        .then(() => {
            getList();
            proxy.$modal.msgSuccess('删除成功');
        })
        .catch(() => {});
}
//禁用有绑定设备的复选框,status:1=未使用，2=已使用
function selectable(row) {
    if (row.status == 2 || isGeneralUser.value) {
        return false;
    }
    return true;
}
//表格增加复制功能
function celldblclick(row, column, cell, event) {
    proxy.$copyText(row[column.property]).then(
        (e) => {
            onCopy();
        },
        function (e) {
            onError();
        }
    );
}
function onCopy() {
    proxy.$notify({
        title: '成功',
        message: '复制成功！',
        type: 'success',
        offset: 50,
        duration: 2000,
    });
}
function onError() {
    proxy.$notify({
        title: '失败',
        message: '复制失败！',
        type: 'error',
        offset: 50,
        duration: 2000,
    });
}

init();
getList();
</script>
