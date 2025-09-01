<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
            <el-form-item label="分类名称" prop="categoryName">
                <el-input v-model="queryParams.categoryName" placeholder="请输入产品分类名称" clearable
                          @keyup.enter.native="handleQuery" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
            <el-form-item style="float:right;">
                <el-button type="primary" plain icon="Plus" @click="handleAdd"
                           v-hasPermi="['iot:category:add']">新增</el-button>
            </el-form-item>
        </el-form>

        <el-table v-loading="loading" :data="categoryList" @selection-change="handleSelectionChange" border>
            <el-table-column label="产品分类名称" align="center" prop="categoryName" />
            <el-table-column label="备注" align="left" header-align="center" prop="remark" min-width="150" />
            <el-table-column label="系统定义" align="center" prop="isSys">
                <template #default="scope">
                    <dict-tag :options="iot_yes_no" :value="scope.row.isSys" />
                </template>
            </el-table-column>
            <el-table-column label="显示顺序" align="center" prop="orderNum" />
            <el-table-column label="创建时间" align="center" prop="createTime" width="180">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
                <template #default="scope">
                    <el-button size="small" style="padding:5px;" icon="Edit"
                               @click="handleUpdate(scope.row)" v-hasPermi="['iot:category:query']"
                               v-if="scope.row.isSys == '0' ? true : !isTenant">修改</el-button>
                    <el-button size="small" style="padding:5px;" icon="Delete"
                               @click="handleDelete(scope.row)" v-hasPermi="['iot:category:remove']"
                               v-if="scope.row.isSys == '0' ? true : !isTenant">删除</el-button>
                    <span style="font-size:10px;color:#999;" v-if="scope.row.isSys == '1' && isTenant">系统定义，不能修改</span>
                </template>
            </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
                    v-model:limit="queryParams.pageSize" @pagination="getList" />

        <!-- 添加或修改产品分类对话框 -->
        <el-dialog title="产品分类" v-model="open" width="500px" append-to-body>
            <el-form ref="categoryRef" :model="form" :rules="rules" label-width="80px">
                <el-form-item label="分类名称" prop="categoryName">
                    <el-input v-model="form.categoryName" placeholder="请输入产品分类名称" />
                </el-form-item>
                <el-form-item label="显示顺序" prop="orderNum">
                    <el-input-number controls-position="right" v-model="form.orderNum"
                                     placeholder="请输入显示顺序" style="width:100%" />
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button type="primary" @click="submitForm" v-hasPermi="['iot:category:edit']"
                               v-show="form.categoryId">修 改</el-button>
                    <el-button type="primary" @click="submitForm" v-hasPermi="['iot:category:add']"
                               v-show="!form.categoryId">新 增</el-button>
                    <el-button @click="cancel">取 消</el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script setup name="Category">
import {
    listCategory,
    getCategory,
    delCategory,
    addCategory,
    updateCategory
} from "@/api/iot/category";
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()
const { proxy } = getCurrentInstance();
const { iot_yes_no } = proxy.useDict("iot_yes_no");

// 是否为租户
const isTenant = ref(false);
const loading = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const showSearch = ref(true);
const total = ref(0);
// 产品分类表格数据
const categoryList = ref([]);
const title = ref("");
const open = ref(false);

const data = reactive({
    // 表单参数
    form: {},
    // 查询参数
    queryParams: {
        pageNum: 1,
        pageSize: 20,
        categoryName: null,
        isSys: null,
    },
    // 表单校验
    rules: {
        categoryName: [{
            required: true,
            message: "产品分类名称不能为空",
            trigger: "blur"
        }, {
            min: 1,
            max: 64,
            message: '产品分类不能少于1个字符和超过64字符',
        }],
        orderNum: [{
            required: true,
            message: '分类顺序不能为空',
            trigger: 'blur',
        }, {
            type: 'number',
            min: -2147483648,
            max: 2147483647,
            message: '顺序值不能超过int型的取值范围(-2^31——2^31-1)',
            trigger: 'blur',
        }],
        remark: [{
            required: false,
            min: 0,
            max: 500,
            message: '备注不能输入超过500个字符',
            trigger: 'blur',
        }],
        isSys: [{
            required: true,
            message: "是否系统通用不能为空",
            trigger: "blur"
        }],
    }
});
const { queryParams, form, rules } = toRefs(data);

function init() {
    if (userStore.roles.indexOf("tenant") !== -1) {
        isTenant.value = true
    }
}
/** 查询产品分类列表 */
function getList() {
    loading.value = true;
    listCategory(queryParams.value).then(response => {
        categoryList.value = response.rows;
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
    form.value = {
        categoryId: null,
        categoryName: null,
        tenantId: null,
        tenantName: null,
        isSys: null,
        parentId: null,
        orderNum: 0,
        delFlag: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null
    };
    proxy.resetForm("categoryRef");
}
/** 搜索按钮操作 */
function handleQuery() {
    queryParams.value.pageNum = 1;
    getList();
}
/** 重置按钮操作 */
function resetQuery() {
    proxy.resetForm("queryRef");
    handleQuery();
}
// 多选框选中数据
function handleSelectionChange(selection) {
    ids.value = selection.map(item => item.categoryId)
    single.value = selection.length !== 1
    multiple.value = !selection.length
}
/** 新增按钮操作 */
function handleAdd() {
    reset();
    open.value = true;
    title.value = "添加产品分类";
}
/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const categoryId = row.categoryId || ids.value
    getCategory(categoryId).then(response => {
        form.value = response.data;
        open.value = true;
        title.value = "修改产品分类";
    });
}
/** 提交按钮 */
function submitForm() {
    proxy.$refs["categoryRef"].validate(valid => {
        if (valid) {
            if (form.value.categoryId != null) {
                updateCategory(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addCategory(form.value).then(response => {
                    proxy.$modal.msgSuccess("新增成功");
                    open.value = false;
                    getList();
                });
            }
        }
    });
}
/** 删除按钮操作 */
function handleDelete(row) {
    const categoryIds = row.categoryId || ids.value;
    let msg = "";
    proxy.$modal.confirm('是否确认删除产品分类编号为"' + categoryIds + '"的数据项？').then(function () {
        return delCategory(categoryIds).then(response => {
            msg = response.msg;
        });
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess(msg);
    }).catch(() => { });
}
/** 导出按钮操作 */
function handleExport() {
    proxy.download('iot/category/export', {
        ...queryParams.value
    }, `category_${new Date().getTime()}.xlsx`)
}

getList();
init();
</script>
