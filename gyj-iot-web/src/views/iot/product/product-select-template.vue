<template>
    <div style="padding-left: 20px">
        <el-form :model="queryParams" ref="product-select-template" :inline="true">
            <el-form-item label="名称" prop="templateName">
                <el-input v-model="queryParams.templateName" placeholder="请输入物模型名称" clearable
                    @keyup.enter.native="handleQuery" />
            </el-form-item>
            <el-form-item label="类别" prop="type">
                <el-select v-model="queryParams.type" placeholder="请选择模型类别" clearable>
                    <el-option v-for="dict in iot_things_type" :key="dict.value" :label="dict.label"
                        :value="dict.value" />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
        </el-form>

        <el-table v-loading="loading" :data="templateList" @selection-change="handleSelectionChange"
            ref="selectTemplateTable" size="small" border>
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="名称" align="center" prop="templateName" />
            <el-table-column label="标识符" align="center" prop="identifier" />
            <el-table-column label="物模型类别" align="center" prop="type">
                <template #default="scope">
                    <dict-tag :options="iot_things_type" :value="scope.row.type" />
                </template>
            </el-table-column>
            <el-table-column label="图表展示" align="center" prop="isChart" width="75">
                <template #default="scope">
                    <dict-tag :options="iot_yes_no" :value="scope.row.isChart" />
                </template>
            </el-table-column>
            <el-table-column label="实时监测" align="center" prop="isMonitor" width="75">
                <template #default="scope">
                    <dict-tag :options="iot_yes_no" :value="scope.row.isMonitor" />
                </template>
            </el-table-column>
            <el-table-column label="只读" align="center" prop="isReadonly" width="75">
                <template #default="scope">
                    <dict-tag :options="iot_yes_no" :value="scope.row.isReadonly" />
                </template>
            </el-table-column>
            <el-table-column label="历史存储" align="center" prop="isHistory" width="75">
                <template #default="scope">
                    <dict-tag :options="iot_yes_no" :value="scope.row.isHistory" />
                </template>
            </el-table-column>
            <el-table-column label="数据类型" align="center" prop="datatype">
                <template #default="scope">
                    <dict-tag :options="iot_data_type" :value="scope.row.datatype" />
                </template>
            </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize"
            @pagination="getList" />
    </div>
</template>

<script setup name="ProductSelectTemplate">
import { listTemplate } from "@/api/iot/template";

const { proxy } = getCurrentInstance();
const { iot_things_type, iot_data_type, iot_yes_no } = proxy.useDict('iot_things_type', 'iot_data_type', 'iot_yes_no');

// 选中数组
const ids = ref([]);
// 非单个禁用
const single = ref(true);
// 非多个禁用
const multiple = ref(true);
// 总条数
const total = ref(0);
// 通用物模型表格数据
const templateList = ref([]);
// 遮罩层
const loading = ref(true);

const data = reactive({
    // 查询参数
    queryParams: {
        pageNum: 1,
        pageSize: 10,
        templateName: null,
        type: null,
    }
})

const { queryParams } = toRefs(data);

/** 查询通用物模型列表 */
function getList() {
    loading.value = true;
    listTemplate(queryParams.value).then((response) => {
        templateList.value = response.rows;
        total.value = response.total;
        loading.value = false;
    });
}
/** 搜索按钮操作 */
function handleQuery() {
    queryParams.value.pageNum = 1;
    getList();
}
/** 重置按钮操作 */
function resetQuery() {
    proxy.resetForm("product-select-template");
    handleQuery();
}
// 多选框选中数据
function handleSelectionChange(selection) {
    ids.value = selection.map((item) => item.templateId);
    single.value = selection.length !== 1;
    multiple.value = !selection.length;
    // Id数组传递到父组件
    proxy.$emit('idsToParentEvent', ids.value);
}

getList();
ids.value = [];
</script>
