<template>
    <el-dialog title="选择产品" v-model="open" width="600px" append-to-body>
        <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="68px">
            <el-form-item label="产品名称" prop="productName">
                <el-input v-model="queryParams.productName" placeholder="请输入产品名称" clearable @keyup.enter.native="handleQuery" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
        </el-form>

        <el-table v-loading="loading" ref="singleTable" :data="productList" @row-click="rowClick" highlight-current-row>
            <el-table-column label="选择" width="50" align="center">
                <template #default="scope">
                    <input type="radio" :checked="scope.row.isSelect" name="product" />
                </template>
            </el-table-column>
            <el-table-column label="产品名称" align="center" prop="productName" />
            <el-table-column label="分类名称" align="center" prop="categoryName" />
            <el-table-column label="租户名称" align="center" prop="tenantName" />
            <el-table-column label="联网方式" align="center" prop="networkMethod">
                <template #default="scope">
                    <dict-tag :options="iot_network_method" :value="scope.row.networkMethod" />
                </template>
            </el-table-column>
            <el-table-column label="创建时间" align="center" prop="createTime" width="100">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
                </template>
            </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
        <template #footer>
            <div class="dialog-footer">
                <el-button @click="confirmSelectProduct" type="primary">确定</el-button>
                <el-button @click="closeDialog" type="info">关 闭</el-button>
            </div>
        </template>
    </el-dialog>
</template>

<script setup name="SipProductList">
import { listProduct } from '@/api/iot/product';

const { proxy } = getCurrentInstance();
const { iot_vertificate_method, iot_network_method } = proxy.useDict('iot_vertificate_method', 'iot_network_method');

// 遮罩层
const loading = ref(true);
// 总条数
const total = ref(0);
// 打开选择产品对话框
const open = ref(false);
// 产品列表
const productList = ref([]);
// 选中的产品编号
const selectProductId = ref(0);
// 选中的产品
const product = ref(null);

const props = defineProps({
    productId: {
        type: Number,
        default: 0,
    }
});

const data = reactive({
    // 查询参数
    queryParams: {
        pageNum: 1,
        pageSize: 10,
        productName: null,
        categoryId: null,
        categoryName: null,
        tenantId: null,
        tenantName: null,
        isSys: null,
        status: 2, //已发布
        deviceType: 3, // 监控设备
        networkMethod: null,
    }
})

const { queryParams } = toRefs(data);

/** 查询产品列表 */
function getList() {
    loading.value = true;
    listProduct(queryParams.value).then((response) => {
        //产品列表初始化isSelect值，用于单选
        for (let i = 0; i < response.rows.length; i++) {
            response.rows[i].isSelect = false;
        }
        productList.value = response.rows;
        total.value = response.total;
        if (selectProductId.value != 0) {
            setRadioSelected(selectProductId.value);
        }
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
    proxy.resetForm('queryForm');
    handleQuery();
}
/** 单选数据 */
function rowClick(info) {
    if (info != null) {
        setRadioSelected(info.productId);
        product.value = info;
    }
}
/** 设置单选按钮选中 */
function setRadioSelected(productId) {
    for (let i = 0; i < productList.value.length; i++) {
        if (productList.value[i].productId == productId) {
            productList.value[i].isSelect = true;
        } else {
            productList.value[i].isSelect = false;
        }
    }
}
/**确定选择产品，产品传递给父组件 */
function confirmSelectProduct() {
    if (product.value == null) {
        proxy.$message.error('请选择产品');
        return;
    }
    proxy.$emit('productEvent', product.value);
    open.value = false;
    product.value = null;
}
/**关闭对话框 */
function closeDialog() {
    open.value = false;
}
// 显示弹框
function show(productId) {
    queryParams.value.pageNum = 1;
    open.value = true;
    selectProductId.value = productId;
    getList();
}

defineExpose({
    show
});
</script>
