<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="请选择设备从机:" label-width="120px" v-if="isSubDev">
        <el-select v-model="queryParams.slaveId" placeholder="请选择设备从机" @change="selectSlave">
          <el-option v-for="slave in slaveList" :key="slave.slaveId" :label="`${slave.deviceName}   (从机地址:${slave.slaveId})`" :value="slave.slaveId"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="日志类型" prop="funType">
        <el-select v-model="queryParams.funType" placeholder="请选择类型" clearable>
          <el-option v-for="dict in iot_function_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="标识符" prop="identify">
        <el-input v-model="queryParams.identify" placeholder="请输入标识符" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker v-model="daterangeTime" style="width: 240px" value-format="yyyy-MM-dd" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"></el-date-picker>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="logList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="showName" align="center" prop="identify" />
      <el-table-column label="指令类型" align="center" prop="funType" width="120px">
        <template #default="scope">
          <dict-tag :options="iot_function_type" :value="scope.row.funType" />
        </template>
      </el-table-column>
      <el-table-column label="设置值" align="center" prop="funValue" />
      <el-table-column label="设备编号" align="center" prop="serialNumber" />
      <el-table-column label="下发时间" align="center" prop="createTime" />
      <el-table-column label="下发结果描述" align="center" prop="resultMsg" />
      <el-table-column label="操作" align="center" width="80">
        <template #default="scope">
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['iot:device:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="DeviceFunc">
import { listLog, delLog } from '@/api/iot/functionLog';

const { proxy } = getCurrentInstance();
const { iot_function_type, iot_yes_no } = proxy.useDict('iot_function_type', 'iot_yes_no');

// 遮罩层
const loading = ref(true);
// 选中数组
const ids = ref([]);
// 非单个禁用
const single = ref(true);
// 非多个禁用
const multiple = ref(true);
// 显示搜索条件
const showSearch = ref(true);
// 总条数
const total = ref(0);
// 设备服务下发日志表格数据
const logList = ref([]);
// 弹出层标题
const title = ref('');
// 是否显示弹出层
const open = ref(false);
//设备数据
const deviceInfo = ref({});
// 时间范围
const daterangeTime = ref([]);
//是否是modbus设备组
const isSubDev = ref(false);
const showName = ref(null);
const slaveList = ref([]);

const data = reactive({
  // 查询参数
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    identify: null,
    funType: null,
    funValue: null,
    messageId: null,
    deviceName: null,
    serialNumber: null,
    mode: null,
    userId: null,
    resultMsg: null,
    resultCode: null,
    slaveId: null,
  },
  // 表单参数
  form: {},
  // 表单校验
  rules: {
    identify: [{ required: true, message: '标识符不能为空', trigger: 'blur' }],
    funType: [{ required: true, message: '功能下发类型不能为空', trigger: 'change' }],
    funValue: [{ required: true, message: '日志值不能为空', trigger: 'blur' }],
    serialNumber: [{ required: true, message: '设备编号不能为空', trigger: 'blur' }],
  }
})

const { queryParams, form, rules } = toRefs(data);

const props = defineProps({
  device: {
    type: Object,
    default: null,
  }
});

// 获取到父组件传递的device后
watch(() => props.device, (newVal, oldVal) => {
  deviceInfo.value = newVal;
  if (deviceInfo.value && deviceInfo.value.deviceId != 0) {
    this.isSubDev = deviceInfo.value.subDeviceList && deviceInfo.value.subDeviceList.length > 0;
    this.showName = this.isSubDev ? '寄存器地址' : '标识符';
    queryParams.value.deviceId = deviceInfo.value.deviceId;
    queryParams.value.slaveId = deviceInfo.value.slaveId;
    queryParams.value.serialNumber = deviceInfo.value.serialNumber;
    slaveList.value = newVal.subDeviceList;
    getList();
  }
});

/** 查询设备服务下发日志列表 */
function getList() {
  loading.value = true;
  if (null != daterangeTime.value && '' != daterangeTime.value) {
    queryParams.value.beginTime = daterangeTime.value[0];
    queryParams.value.endTime = daterangeTime.value[1];
  }
  if (queryParams.value.slaveId) {
    queryParams.value.serialNumber = queryParams.value.serialNumber + '_' + queryParams.value.slaveId;
  }
  listLog(queryParams.value).then((response) => {
    logList.value = response.rows;
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
    id: null,
    identify: null,
    funType: null,
    funValue: null,
    messageId: null,
    deviceName: null,
    serialNumber: null,
    mode: null,
    userId: null,
    resultMsg: null,
    resultCode: null,
    createBy: null,
    createTime: null,
    remark: null,
  };
  proxy.resetForm('form');
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
// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}
/** 删除按钮操作 */
function handleDelete(row) {
  const ids = row.id || ids.value;
  proxy.$modal
    .confirm('是否确认删除设备服务下发日志编号为"' + ids + '"的数据项？')
    .then(function () {
      return delLog(ids);
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess('删除成功');
    })
    .catch(() => {});
}
/** 导出按钮操作 */
function handleExport() {
  proxy.download('iot/log/export', {
      ...queryParams.value,
    },
    `log_${new Date().getTime()}.xlsx`
  );
}
//选择从机
function selectSlave() {}

queryParams.value.serialNumber = proxy.device.serialNumber;
getList();
</script>
