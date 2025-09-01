<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
            <el-form-item label="脚本标识" prop="scriptId">
                <el-input v-model="queryParams.scriptId" placeholder="请输入脚本名" clearable
                          @keyup.enter.native="handleQuery" />
            </el-form-item>
            <el-form-item label="脚本名" prop="scriptName">
                <el-input v-model="queryParams.scriptName" placeholder="请输入脚本名" clearable
                          @keyup.enter.native="handleQuery" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
            <el-form-item style="float: right">
                <el-button type="primary" plain icon="Plus" @click="handleAdd"
                           v-hasPermi="['iot:script:add']">新增</el-button>
            </el-form-item>
        </el-form>

        <el-table v-loading="loading" :data="scriptList" @selection-change="handleSelectionChange">
            <el-table-column label="脚本名称" align="center" prop="scriptName" />
            <el-table-column label="所属产品" align="center" prop="productName" />
            <el-table-column label="脚本标识" align="center" prop="scriptId" width="200" />
            <el-table-column label="脚本事件" align="center" prop="status">
                <template #default="scope">
                    <dict-tag :options="rule_script_event" :value="scope.row.scriptEvent" size="small" />
                </template>
            </el-table-column>
            <el-table-column label="脚本动作" align="center" prop="status">
                <template #default="scope">
                    <dict-tag :options="rule_script_action" :value="scope.row.scriptAction"
                              size="small" />
                </template>
            </el-table-column>
            <el-table-column label="脚本语言" align="center" prop="scriptLanguage" />
            <el-table-column label="状态" align="center" prop="enable" width="120">
                <template #default="scope">
                    <el-tag v-if="scope.row.enable == 1" type="success" size="small">启动</el-tag>
                    <el-tag v-if="scope.row.enable == 0" type="danger" size="small">暂停</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="执行顺序" align="center" prop="scriptOrder" />
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="250">
                <template #default="scope">
                    <el-button size="small" style="padding:5px" icon="Calendar" @click="handleLog(scope.row.scriptId)" v-hasPermi="['iot:script:query']">日志</el-button>
                    <el-button size="small" style="padding:5px" icon="View" @click="handleUpdate(scope.row)"
                               v-hasPermi="['iot:script:query']">查看</el-button>
                    <el-button size="small" style="padding:5px" icon="Delete" @click="handleDelete(scope.row)"
                               v-hasPermi="['iot:script:remove']">删除</el-button>
                </template>
            </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum"
                    :limit.sync="queryParams.pageSize" @pagination="getList" />

        <!-- 添加或修改规则引擎脚本对话框 -->
        <el-dialog :title="title" v-model="open" width="800px" append-to-body :close-on-click-modal="false" :close-on-press-escape="false">
            <el-form ref="scriptRef" :model="form" :rules="rules" label-width="90px">
                <el-row :gutter="50">
                    <el-col :span="12">
                        <el-form-item label="脚本名称" prop="scriptName">
                            <el-input v-model="form.scriptName" placeholder="请输入脚本名" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="执行顺序" prop="scriptOrder">
                            <el-input-number v-model="form.scriptOrder" placeholder="请输入脚本名" type="number"
                                             controls-position="right" style="width: 100%" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="脚本事件" prop="scriptEvent">
                            <el-select v-model="form.scriptEvent" placeholder="请选择脚本事件" style="width: 100%">
                                <el-option v-for="dict in rule_script_event" :key="dict.label"
                                           :label="dict.label" :value="Number(dict.value)"
                                           :disabled="dict.value !== '1' && dict.value !== '2'"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>

                    <el-col :span="12">
                        <el-form-item label="脚本动作" prop="scriptAction">
                            <el-select v-model="form.scriptAction" placeholder="请选择脚本动作" style="width: 100%">
                                <el-option v-for="dict in rule_script_action" :key="dict.label"
                                           :label="dict.label" :value="Number(dict.value)"
                                           :disabled="dict.value !== '1'"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="脚本状态" prop="enable">
                            <el-switch v-model="form.enable" :active-value="1" :inactive-value="0" disabled />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="所属产品" prop="productName">
                            <el-input readonly v-model="form.productName" size="small" placeholder="请选择产品" style="margin-top: 3px">
                                <template #append>
                                    <el-button @click="handleSelectProduct()" size="small">选择产品</el-button>
                                </template>
                            </el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12" style="float: right"></el-col>
                </el-row>
            </el-form>
            <div style="padding: 0px 10px" @click="editClick">
                <AceEditor ref="codeEditor" v-model:content="form.scriptData" lang="groovy" codeStyle="chrome"
                           :read-only="false" width="100%" height="450px"></AceEditor>
            </div>
            <div style="padding: 0 10px; margin: 10px 0">
                <el-alert :title="validateMsg" type="success" show-icon v-if="isValidate && validateMsg"
                          :closable="false"></el-alert>
                <el-alert :title="validateMsg" type="error" show-icon v-if="!isValidate && validateMsg"
                          :closable="false"></el-alert>
            </div>
            <template #footer>
                <div class="dialog-footer">
                    <el-button type="success" @click="handleValidate">验 证</el-button>
                    <el-button type="primary" @click="submitForm" v-hasPermi="['iot:script:edit']"
                               v-show="form.scriptId" :disabled="!isValidate">修
                        改</el-button>
                    <el-button type="primary" @click="submitForm" v-hasPermi="['iot:script:add']"
                               v-show="!form.scriptId" :disabled="!isValidate">新
                        增</el-button>
                    <el-button @click="cancel">取 消</el-button>
                </div>
            </template>
        </el-dialog>

        <el-dialog :title="title" v-model="openLog" width="700px" append-to-body :close-on-click-modal="false" :close-on-press-escape="false">
            <div
                ref="logContainer"
                v-loading="logLoading"
                element-loading-text="加载中"
                element-loading-spinner="el-icon-loading"
                element-loading-background="rgba(0, 0, 0, 0.8)"
                style="border: 1px solid #ccc; border-radius: 4px; height: 450px; background-color: #181818; color: #fff; padding: 10px; line-height: 20px; overflow: auto">
                <pre>
                    {{ logs }}
                </pre>
            </div>
            <div slot="footer" class="dialog-footer">
                <el-button @click="cancelLog">关闭</el-button>
            </div>
        </el-dialog>

        <!-- 产品列表 -->
        <product-list ref="productListRef" @productEvent="getSelectProduct($event)"></product-list>
    </div>
</template>

<script setup name="Script">
import { listScript, getScript, getScriptLog, delScript, addScript, updateScript, validateScript } from '@/api/iot/script';
import AceEditor from '@/views/components/editor/editor.vue';
import productList from './product-list';

const { proxy } = getCurrentInstance();

const { rule_script_type,rule_script_language,rule_script_event,rule_script_action } = proxy.useDict('rule_script_type',
    'rule_script_language', 'rule_script_event', 'rule_script_action');

// 日志
const logs = ref('');
// 日志遮罩层
const logLoading = ref(true);
// 是否显示日志弹窗
const openLog = ref(false);
// 脚本数据验证
const isValidate = ref(false);
// 脚本数据验证结果
const validateMsg = ref('');
// 遮罩层
const loading = ref(true);
// 选中数组
const scriptIds = ref([]);
// 非单个禁用
const single = ref(true);
// 非多个禁用
const multiple = ref(true);
// 显示搜索条件
const showSearch = ref(true);
// 总条数
const total = ref(0);
// 规则引擎脚本表格数据
const scriptList = ref([]);
// 弹出层标题
const title = ref('');
// 是否显示弹出层
const open = ref(false);

const data = reactive({
    // 表单参数
    form: {},
    // 查询参数
    queryParams: {
        pageNum: 1,
        pageSize: 10,
        scriptPurpose: 1, // 只展示数据流(1=数据流，2=触发器，3=执行动作)
        scriptId: null,
        scriptName: null,
        scriptData: null,
        scriptType: null,
        scriptLanguage: null,
        enable: null,
    },
    // 表单校验
    rules: {
        scriptId: [{ required: true, message: '脚本标识不能为空', trigger: 'blur' }],
        productName: [{ required: true, message: '所属产品不能为空', trigger: 'blur' }],
        scriptName: [{ required: true, message: '脚本名不能为空', trigger: 'blur' }],
        scriptType: [{ required: true, message: '脚本类型不能为空', trigger: 'change' }],
        scriptLanguage: [{ required: true, message: '脚本语言不能为空', trigger: 'change' }],
        scriptEvent: [{ required: true, message: '', trigger: 'change' }],
        scriptAction: [{ required: true, message: '', trigger: 'change' }],
        scriptOrder: [{ required: true, message: '', trigger: 'change' }],
        enable: [{ required: true, message: '状态不能为空', trigger: 'blur' }],
    },
})

const { queryParams, form, rules } = toRefs(data);

/** 查询规则引擎脚本列表 */
function getList() {
    loading.value = true;
    listScript(queryParams.value).then((response) => {
        scriptList.value = response.rows;
        total.value = response.total;
        loading.value = false;
    });
}
// 取消按钮
function cancel() {
    open.value = false;
    reset();
}
// 取消日志按钮
function cancelLog() {
    logs.value = '';
    openLog.value = false;
}
// 表单重置
function reset() {
    validateMsg.value = '';
    isValidate.value = false;
    form.value = {
        id: null,
        applicationName: 'gyjiot', // 后端配置和规则/脚本需要一致
        scriptId: null,
        productId: null,
        productName: '',
        scriptName: null,
        scriptType: 'script',
        remark: null,
        scriptLanguage: 'groovy',
        enable: 1,
        scriptPurpose: 1, // 脚本用途(1=数据流，2=触发器，3=执行动作)
        scriptOrder: 1, // 脚本执行顺序，数字越大越先执行
        scriptAction: 1, // 脚本动作(1=消息重发，2=消息通知，3=Http推送，4=Mqtt桥接，5=数据库存储)
        scriptEvent: 1, // 脚本事件(1=设备上报，2=平台下发，3=设备上线，4=设备离线)
        sceneId: 0,
        scriptData: '',
    };
    proxy.resetForm('scriptRef');
}

/** 搜索按钮操作 */
function handleQuery() {
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
    scriptIds.value = selection.map((item) => item.scriptId);
    single.value = selection.length !== 1;
    multiple.value = !selection.length;
}
/** 新增按钮操作 */
function handleAdd() {
    reset();
    open.value = true;
    title.value = '编辑规则脚本';
}
/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const scriptId = row.scriptId || scriptIds.value;
    getScript(scriptId).then((response) => {
        form.value = response.data;
        open.value = true;
        title.value = '修改规则引擎脚本';
    });
}
/** 日志按钮操作 */
function handleLog(scriptId) {
    logLoading.value = true;
    getScriptLog(scriptId).then((response) => {
        logs.value = response.msg;
        form.value.scriptId = scriptId;
        openLog.value = true;
        title.value = "脚本日志";
        logLoading.value = false;
        // 滑动到底部
        proxy.$nextTick(function () {
            let messageContent = proxy.$refs.logContainer;
            messageContent.scroll({
                top: messageContent.scrollHeight,
            });
        });
    });
}
/** 日志刷新操作 */
function refreshLog() {
    handleLog(form.value.scriptId);
}
/**选择产品 */
function handleSelectProduct() {
    proxy.$refs['productListRef'].show(form.value.productId);
}
function getSelectProduct(data) {
    form.value.productId = data.productId;
    form.value.productName = data.productName;
}
/** 提交按钮 */
function submitForm() {
    proxy.$refs['scriptRef'].validate((valid) => {
        if (valid) {
            if (form.value.scriptId != null) {
                updateScript(form.value).then((response) => {
                    proxy.$modal.msgSuccess('修改成功');
                    open.value = false;
                    getList();
                });
            } else {
                addScript(form.value).then((response) => {
                    proxy.$modal.msgSuccess('新增成功');
                    open.value = false;
                    getList();
                });
            }
        }
    });
}
/** 删除按钮操作 */
function handleDelete(row) {
    const scriptIds = row.scriptId || scriptIds.value;
    proxy.$modal.confirm('是否确认删除规则引擎脚本编号为"' + scriptIds + '"的数据项？')
        .then(function () {
            return delScript(scriptIds);
        })
        .then(() => {
            getList();
            proxy.$modal.msgSuccess('删除成功');
        })
        .catch(() => { });
}
/** 验证按钮操作 */
function handleValidate() {
    validateMsg.value = '';
    isValidate.value = false;
    validateScript(form.value).then((response) => {
        isValidate.value = response.data;
        validateMsg.value = response.msg;
    });
}
/** 编辑器单机事件 */
function editClick() {
    validateMsg.value = '';
    isValidate.value = false;
}
/** 导出按钮操作 */
function handleExport() {
    this.download('iot/script/export', {
            ...queryParams.value,
        },
        `script_${new Date().getTime()}.xlsx`
    );
}

getList();
</script>
