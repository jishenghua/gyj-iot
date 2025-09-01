<template>
  <div style="padding-left: 20px">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="70px">
      <el-form-item label="定时名称" prop="jobName">
        <el-input v-model="queryParams.jobName" placeholder="请输入定时名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="定时状态" prop="status" style="margin-left: 20px">
        <el-select v-model="queryParams.status" placeholder="请选择定时状态" clearable>
          <el-option v-for="dict in sys_job_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
      <el-form-item style="float: right">
        <el-button type="primary" plain icon="Plus" @click="handleAdd"
          v-hasPermi="['iot:device:timer']">新增</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="jobList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="名称" align="center" prop="jobName" :show-overflow-tooltip="true" />
      <el-table-column label="描述" align="center" prop="cronText">
        <template #default="scope">
          <div v-html="formatCronDisplay(scope.row)"></div>
        </template>
      </el-table-column>
      <el-table-column label="CRON表达式" align="center" prop="cronExpression" :show-overflow-tooltip="true" />
      <el-table-column label="动作" align="left" prop="actions" :show-overflow-tooltip="true">
        <template #default="scope">
          <div v-html="formatActionsDisplay(scope.row.actions)" style="overflow: hidden; white-space: nowrap"></div>
        </template>
      </el-table-column>

      <el-table-column label="状态" align="center">
        <template #default="scope">
          <el-switch v-model="scope.row.status" active-value="0" inactive-value="1" active-text="启用"
            @change="handleStatusChange(scope.row)"></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button type="text" icon="Edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['iot:device:timer']">修改</el-button>
          <el-button type="text" icon="el-icon-caret-right" @click="handleView(scope.row)"
            v-hasPermi="['iot:device:timer']">定时详细</el-button>
          <br />
          <el-button type="text" icon="delete" @click="handleDelete(scope.row)"
            v-hasPermi="['iot:device:timer']">删除</el-button>
          <el-button type="text" icon="el-icon-caret-right" @click="handleRun(scope.row)"
            v-hasPermi="['iot:device:timer']">执行一次</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改定时定时对话框 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="定时名称" prop="jobName">
          <el-input v-model="form.jobName" placeholder="请输入定时名称" style="width: 340px" />
        </el-form-item>
        <el-form-item label="执行时间" required>
          <el-time-picker v-model="timerTimeValue" value-format="HH:mm" format="HH:mm" placeholder="选择时间" :editable="false"
            style="width: 340px" @change="timeChange" :disabled="form.isAdvance == 1"></el-time-picker>
        </el-form-item>
        <el-form-item label="选择星期" prop="timerWeek">
          <el-row>
            <el-col :span="18">
              <el-select v-model="timerWeekValue" placeholder="请选择" multiple style="width: 100%" @change="weekChange"
                :disabled="form.isAdvance == 1">
                <el-option v-for="item in timerWeeks" :key="item.value" :label="item.label"
                  :value="item.value"></el-option>
              </el-select>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="cron表达式" prop="cron">
          <el-row>
            <el-col :span="18">
              <el-input v-model="form.cronExpression" placeholder="cron执行表达式" :disabled="form.isAdvance == 0">
                <template slot="append">
                  <el-button type="primary" @click="handleShowCron" :disabled="form.isAdvance == 0">
                    生成表达式
                    <i class="el-icon-time el-icon--right"></i>
                  </el-button>
                </template>
              </el-input>
            </el-col>
            <el-col :span="4" :offset="1">
              <el-checkbox v-model="form.isAdvance" :true-label="1" :false-label="0"
                @change="customerCronChange">自定义表达式</el-checkbox>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="定时状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in sys_job_status" :key="dict.value" :label="dict.value">{{ dict.label
            }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <div style="padding-bottom: 15px; padding: 0 20px">
          <el-divider></el-divider>
        </div>
        <el-form-item label="执行动作" required>
          <el-row v-for="(actionItem, index) in actionList" :key="index + 'action'" style="margin-bottom: 10px">
            <el-col :span="4">
              <el-select v-model="actionItem.type" placeholder="请选择类别" @change="actionTypeChange($event, index)">
                <el-option v-for="(subItem, subIndex) in modelTypes" :key="subIndex + 'type'" :label="subItem.label"
                  :value="subItem.value"></el-option>
              </el-select>
            </el-col>
            <el-col :span="4" :offset="1">
              <el-select v-model="actionItem.id" placeholder="请选择" v-if="actionItem.type == 1"
                @change="thingsModelItemChange($event, index)">
                <el-option v-for="(subItem, subIndex) in thingsModel.properties" :key="subIndex + 'property'"
                  :label="subItem.name" :value="subItem.id"></el-option>
              </el-select>
              <el-select v-model="actionItem.id" placeholder="请选择" v-else-if="actionItem.type == 2"
                @change="thingsModelItemChange($event, index)">
                <el-option v-for="(subItem, subIndex) in thingsModel.functions" :key="subIndex + 'func'"
                  :label="subItem.name" :value="subItem.id"></el-option>
              </el-select>
              <el-select v-model="form.id" placeholder="请选择" v-else-if="actionItem.type == 3"
                @change="thingsModelItemChange($event, index)">
                <el-option v-for="(subItem, subIndex) in thingsModel.functions" :key="subIndex + 'func'"
                  :label="subItem.name" :value="subItem.id"></el-option>
              </el-select>
            </el-col>
            <el-col :span="10" :offset="1">
              <!--物模型项的值-->
              <span
                v-if="actionItem.thingsModelItem && (actionItem.thingsModelItem.datatype.type == 'integer' || actionItem.thingsModelItem.datatype.type == 'decimal')">
                <el-input v-model="actionItem.value" placeholder="值" :max="actionItem.thingsModelItem.datatype.max"
                  :min="actionItem.thingsModelItem.datatype.min" type="number" size="small">
                  <template slot="append">{{ actionItem.thingsModelItem.datatype.unit }}</template>
                </el-input>
              </span>
              <span v-else-if="actionItem.thingsModelItem && actionItem.thingsModelItem.datatype.type == 'bool'">
                <el-switch v-model="actionItem.value" :active-text="actionItem.thingsModelItem.datatype.trueText"
                  :inactive-text="actionItem.thingsModelItem.datatype.falseText" active-value="1"
                  inactive-value="0"></el-switch>
              </span>
              <span v-else-if="actionItem.thingsModelItem && actionItem.thingsModelItem.datatype.type == 'enum'">
                <el-select v-model="actionItem.value" placeholder="请选择" style="width: 100%">
                  <el-option v-for="(subItem, subIndex) in actionItem.thingsModelItem.datatype.enumList"
                    :key="subIndex + 'things'" :label="subItem.text" :value="subItem.value"></el-option>
                </el-select>
              </span>
              <span v-else-if="actionItem.thingsModelItem && actionItem.thingsModelItem.datatype.type == 'string'">
                <el-input v-model="actionItem.value" placeholder="请输入字符串"
                  :max="actionItem.thingsModelItem.datatype.maxLength" />
              </span>
              <span v-else-if="actionItem.thingsModelItem && actionItem.thingsModelItem.datatype.type == 'array'">
                <el-input v-model="actionItem.value" placeholder="请输入英文逗号分隔的数组" />
              </span>
            </el-col>
            <el-col :span="2" :offset="1" v-if="index != 0"><a style="color: #f56c6c"
                @click="removeEnumItem(index)">删除</a></el-col>
          </el-row>
          <div>
            +
            <a style="color: #409eff" @click="addEnumItem()">添加执行动作</a>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm" :loading="submitButtonLoading">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="Cron表达式生成器" v-model="openCron" append-to-body destroy-on-close class="scrollbar">
      <crontab @hide="openCron = false" @fill="crontabFill" :expression="expression" style="padding-bottom: 80px">
      </crontab>
    </el-dialog>

    <!-- 定时日志详细 -->
    <el-dialog title="定时详细" v-model="openView" width="700px" append-to-body>
      <el-form ref="form" :model="form" label-width="120px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="定时编号：">{{ form.jobId }}</el-form-item>
            <el-form-item label="定时名称：">{{ form.jobName }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="定时分组：">{{ jobGroupFormat(form) }}</el-form-item>
            <el-form-item label="创建时间：">{{ form.createTime }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否并发：">
              <div v-if="form.concurrent == 0">允许</div>
              <div v-else-if="form.concurrent == 1">禁止</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="cron表达式：">{{ form.cronExpression }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="执行策略：">
              <div v-if="form.misfirePolicy == 0">默认策略</div>
              <div v-else-if="form.misfirePolicy == 1">立即执行</div>
              <div v-else-if="form.misfirePolicy == 2">执行一次</div>
              <div v-else-if="form.misfirePolicy == 3">放弃执行</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下次执行时间：">{{ parseTime(form.nextValidTime) }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="定时状态：">
              <div v-if="form.status == 0">正常</div>
              <div v-else-if="form.status == 1">暂停</div>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="执行动作：">
              <div v-html="formatActionsDisplay(form.actions)"
                style="border: 1px solid #ddd; padding: 10px; border-radius: 5px; width: 465px"></div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="openView = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DeviceTimer">
import { listJob, getJob, delJob, addJob, updateJob, runJob, changeJobStatus } from '@/api/iot/deviceJob';
import Crontab from '@/components/Crontab';

const { proxy } = getCurrentInstance();
const { sys_job_group, sys_job_status } = proxy.useDict('sys_job_group', 'sys_job_status');

// 物模型JSON
const thingsModel = ref({});
// 动作列表
const actionList = ref([]);
// 设备
const deviceInfo = ref({});
// 遮罩层
const loading = ref(false);
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
// 定时定时表格数据
const jobList = ref([]);
// 弹出层标题
const title = ref('');
// 是否显示弹出层
const open = ref(false);
// 是否显示详细弹出层
const openView = ref(false);
// 是否显示Cron表达式弹出层
const openCron = ref(false);
// 传入的表达式
const expression = ref('');
// 提交按钮加载
const submitButtonLoading = ref(false);
const timerWeekValue = ref([1, 2, 3, 4, 5, 6, 7]);
// 时间
const timerTimeValue = ref('');

const data = reactive({
  // 查询参数
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    deviceId: 0,
    jobName: undefined,
    jobGroup: undefined,
    status: undefined,
  },
  // 周
  timerWeeks: [
    {
      value: 1,
      label: '周一',
    },
    {
      value: 2,
      label: '周二',
    },
    {
      value: 3,
      label: '周三',
    },
    {
      value: 4,
      label: '周四',
    },
    {
      value: 5,
      label: '周五',
    },
    {
      value: 6,
      label: '周六',
    },
    {
      value: 7,
      label: '周日',
    },
  ],
  // 物模型类别
  modelTypes: [
    {
      value: 1,
      label: '属性',
    },
    {
      value: 2,
      label: '功能',
    },
  ],
  // 表单参数
  form: {},
  // 表单校验
  rules: {
    jobName: [
      {
        required: true,
        message: '定时名称不能为空',
        trigger: 'blur',
      },
    ],
  }
})

const { queryParams, timerWeeks, modelTypes, form, rules } = toRefs(data);

const props = defineProps({
  device: {
    type: Object,
    default: null,
  }
});

// 获取到父组件传递的productId后，刷新列表
watch(() => props.device, (newVal, oldVal) => {
  deviceInfo.value = newVal;
  if (deviceInfo.value && deviceInfo.value.deviceId != 0) {
    thingsModel.value = deviceInfo.value.cacheThingsModel;
    queryParams.value.deviceId = deviceInfo.value.deviceId;
  }
});

/** 查询定时定时列表 */
function getList() {
  loading.value = true;
  listJob(queryParams.value).then((response) => {
    jobList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}
// 定时组名字典翻译
function jobGroupFormat(row, column) {
  return proxy.selectDictLabel(sys_job_group, row.jobGroup);
}
// 取消按钮
function cancel() {
  open.value = false;
  reset();
}
// 表单重置
function reset() {
  form.value = {
    jobId: undefined,
    jobName: undefined,
    cronExpression: undefined,
    status: '0',
    jobGroup: 'DEFAULT', // 定时分组
    misfirePolicy: 2, // 1=立即执行，2=执行一次，3=放弃执行
    concurrent: 1, // 是否并发，1=禁止，0=允许
    isAdvance: 0, // 是否详细cron表达式
    jobType: 1, // 任务类型 1=设备定时，2=设备告警，3=场景联动
    productId: 0,
    productName: '',
    sceneId: 0, //场景ID
    alertId: 0, // 告警ID
    actions: '',
  };
  submitButtonLoading.value = false;
  timerWeekValue.value = [1, 2, 3, 4, 5, 6, 7];
  timerTimeValue.value = '';
  actionList.value = [
    {
      id: '',
      name: '',
      value: '',
      type: 2, // 1=属性，2=功能，3=事件，5=设备上线，6=设备下线
      deviceId: deviceInfo.value.deviceId,
      deviceName: deviceInfo.value.deviceName,
      thingsModelItem: {
        id: '',
        name: '',
        datatype: {
          type: '',
        },
      },
    },
  ];
  // 物模型项,对应actions
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
  ids.value = selection.map((item) => item.jobId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}
// 定时状态修改
function handleStatusChange(row) {
  let text = row.status === '0' ? '启用' : '停用';
  proxy.$modal.confirm('确认要"' + text + '""' + row.jobName + '"定时吗？')
    .then(function () {
      return changeJobStatus(row.jobId, row.status);
    })
    .then(() => {
      proxy.$modal.msgSuccess(text + '成功');
    })
    .catch(function () {
      row.status = row.status === '0' ? '1' : '0';
    });
}
/* 立即执行一次 */
function handleRun(row) {
  proxy.$modal.confirm('确认要立即执行一次"' + row.jobName + '"定时吗？')
    .then(function () {
      return runJob(row.jobId, row.jobGroup);
    })
    .then(() => {
      proxy.$modal.msgSuccess('执行成功');
    })
    .catch(() => { });
}
/** 定时详细信息 */
function handleView(row) {
  getJob(row.jobId).then((response) => {
    form.value = response.data;
    openView.value = true;
  });
}
/** cron表达式按钮操作 */
function handleShowCron() {
  expression.value = form.value.cronExpression;
  openCron.value = true;
}
/** 确定后回传值 */
function crontabFill(value) {
  form.value.cronExpression = value;
}
/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = '添加定时';
}
/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const jobId = row.jobId || ids.value;
  getJob(jobId).then((response) => {
    form.value = response.data;
    // actionList赋值
    actionList.value = JSON.parse(form.value.actions);
    for (let i = 0; i < actionList.value.length; i++) {
      if (actionList.value[i].type == 1) {
        for (let j = 0; j < thingsModel.value.properties.length; j++) {
          if (actionList.value[i].id == thingsModel.value.properties[j].id) {
            actionList.value[i].thingsModelItem = thingsModel.value.properties[j];
            break;
          }
        }
      } else if (actionList.value[i].type == 2) {
        for (let j = 0; j < thingsModel.value.functions.length; j++) {
          if (actionList.value[i].id == thingsModel.value.functions[j].id) {
            actionList.value[i].thingsModelItem = thingsModel.value.functions[j];
            break;
          }
        }
      }
    }
    if (form.value.isAdvance == 0) {
      let arrayValue = form.value.cronExpression.substring(12).split(',').map(Number);
      timerWeekValue.value = arrayValue;
      timerTimeValue.value = form.value.cronExpression.substring(5, 7) + ':' + form.value.cronExpression.substring(2, 4);
    }
    open.value = true;
    title.value = '修改定时';
  });
}
/** 提交按钮 */
function submitForm() {
  proxy.$refs['form'].validate((valid) => {
    if (valid) {
      // 验证不能为空
      if (form.value.isAdvance == 0) {
        if (timerTimeValue.value == '' || timerTimeValue.value == null) {
          proxy.$modal.alertError('执行时间不能空');
          return;
        }
        if (timerWeekValue.value == null || timerWeekValue.value == '') {
          proxy.$modal.alertError('请选择要执行的星期');
          return;
        }
      } else if (form.value.isAdvance == 1) {
        if (form.value.cronExpression == '') {
          proxy.$modal.alertError('cron表达式不能为空');
          return;
        }
      }
      for (let i = 0; i < actionList.value.length; i++) {
        if (actionList.value[i].id == '' || actionList.value[i].name == '' || actionList.value[i].value == '') {
          proxy.$modal.alertError('执行动作中的选项和值不能为空');
          return;
        }
      }
      // 动作
      actionList.value[0].deviceId = deviceInfo.value.deviceId;
      actionList.value[0].deviceName = deviceInfo.value.deviceName;
      // 删除对象中的物模型属性
      for (let i = 0; i < actionList.value.length; i++) {
        proxy.$delete(actionList.value[i], 'thingsModelItem');
      }
      form.value.actions = JSON.stringify(actionList.value);
      // 设备信息
      form.value.deviceId = deviceInfo.value.deviceId;
      form.value.deviceName = deviceInfo.value.deviceName;
      form.value.serialNumber = deviceInfo.value.serialNumber;
      form.value.productId = deviceInfo.value.productId;
      form.value.productName = deviceInfo.value.productName;
      // 按钮等待后端加载完
      this.submitButtonLoading = true;
      if (form.value.jobId != undefined) {
        updateJob(form.value).then((response) => {
          proxy.$modal.msgSuccess('修改成功');
          submitButtonLoading.value = false;
          open.value = false;
          getList();
        });
      } else {
        addJob(form.value).then((response) => {
          proxy.$modal.msgSuccess('新增成功');
          submitButtonLoading.value = false;
          open.value = false;
          getList();
        });
      }
    }
  });
}
/** 删除按钮操作 */
function handleDelete(row) {
  const jobIds = row.jobId || ids.value;
  proxy.$modal.confirm('是否确认删除定时定时编号为"' + jobIds + '"的数据项？')
    .then(function () {
      return delJob(jobIds);
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess('删除成功');
    })
    .catch(() => { });
}
/** 导出按钮操作 */
function handleExport() {
  proxy.download('iot/job/export', {
      ...queryParams.value,
    },
    `job_${new Date().getTime()}.xlsx`
  );
}

/** 添加枚举项 */
function addEnumItem() {
  actionList.value.push({
    id: '',
    name: '',
    value: '',
    type: 2, // 1=属性，2=功能，3=事件，5=设备上线，6=设备下线
    deviceId: deviceInfo.value.deviceId,
    deviceName: deviceInfo.value.deviceName,
    thingsModelItem: {
      id: '',
      name: '',
      datatype: {
        type: '',
      },
    },
  });
}
/** 删除枚举项 */
function removeEnumItem(index) {
  actionList.value.splice(index, 1);
}
/** 星期改变事件 **/
function weekChange(data) {
  gentCronExpression();
}
/** 时间改变事件 **/
function timeChange(data) {
  gentCronExpression();
}
/**自定义cron表达式选项改变事件 */
function customerCronChange(data) {
  if (data == 0) {
    gentCronExpression();
  }
}
/** 生成cron表达式**/
function gentCronExpression() {
  let hour = '00';
  let minute = '00';
  if (timerTimeValue.value != null && timerTimeValue.value != '') {
    hour = timerTimeValue.value.substring(0, 2);
    minute = timerTimeValue.value.substring(3);
  }
  let week = '*';
  if (timerWeekValue.value.length > 0) {
    week = timerWeekValue.value.sort();
  }
  form.value.cronExpression = '0 ' + minute + ' ' + hour + ' ? * ' + week;
}

/** 动作类型改变事件 **/
function actionTypeChange(data, index) {
  actionList.value[index].id = '';
  actionList.value[index].value = '';
  actionList.value[index].thingsModelItem = null;
}
/** 物模型项改变事件 **/
function thingsModelItemChange(identifier, index) {
  actionList.value[index].value = '';
  if (actionList.value[index].type == 1) {
    //属性
    for (let i = 0; i < thingsModel.value.properties.length; i++) {
      if (thingsModel.value.properties[i].id == identifier) {
        actionList.value[index].name = thingsModel.value.properties[i].name;
        actionList.value[index].thingsModelItem = thingsModel.value.properties[i];
        break;
      }
    }
  } else if (actionList.value[index].type == 2) {
    //事件
    for (let i = 0; i < thingsModel.value.functions.length; i++) {
      if (thingsModel.value.functions[i].id == identifier) {
        actionList.value[index].name = thingsModel.value.functions[i].name;
        actionList.value[index].thingsModelItem = thingsModel.value.functions[i];
        break;
      }
    }
  }
}
/** 格式化显示动作 */
function formatActionsDisplay(json) {
  if (json == null || json == '') {
    return;
  }
  let actions = JSON.parse(json);
  let result = '';
  for (let i = 0; i < actions.length; i++) {
    let value = actions[i].value;
    if (actions[i].type == 1) {
      // 属性
      for (let j = 0; j < thingsModel.value.properties.length; j++) {
        if (actions[i].id == thingsModel.value.properties[j].id) {
          if (thingsModel.value.properties[j].datatype.type == 'decimal' || thingsModel.value.properties[j].datatype.type == 'integer') {
            value = actions[i].value + thingsModel.value.properties[j].datatype.unit;
          } else if (thingsModel.value.properties[j].datatype.type == 'enum') {
            for (let k = 0; k < thingsModel.value.properties[j].datatype.enumList.length; k++) {
              if (actions[i].value == thingsModel.value.properties[j].datatype.enumList[k].value) {
                value = thingsModel.value.properties[j].datatype.enumList[k].text;
                break;
              }
            }
          } else if (thingsModel.value.properties[j].datatype.type == 'bool') {
            value = actions[i].value == '1' ? thingsModel.value.properties[j].datatype.trueText : thingsModel.value.properties[j].datatype.falseText;
          }
          break;
        }
      }
    } else if (actions[i].type == 2) {
      // 功能
      for (let j = 0; j < thingsModel.value.functions.length; j++) {
        if (actions[i].id == thingsModel.value.functions[j].id) {
          if (thingsModel.value.functions[j].datatype.type == 'decimal' || thingsModel.value.functions[j].datatype.type == 'integer') {
            value = actions[i].value + thingsModel.value.functions[j].datatype.unit;
          } else if (thingsModel.value.functions[j].datatype.type == 'enum') {
            for (let k = 0; k < thingsModel.value.functions[j].datatype.enumList.length; k++) {
              if (actions[i].value == thingsModel.value.functions[j].datatype.enumList[k].value) {
                value = thingsModel.value.functions[j].datatype.enumList[k].text;
                break;
              }
            }
          } else if (thingsModel.value.functions[j].datatype.type == 'bool') {
            value = actions[i].value == '1' ? thingsModel.value.functions[j].datatype.trueText : thingsModel.value.functions[j].datatype.falseText;
          }
          break;
        }
      }
    }
    result = result + actions[i].name + '：<span style="color:#F56C6C">' + value + '</span><br />';
  }
  return result;
}
/** 格式化显示CRON描述 */
function formatCronDisplay(item) {
  let result = '';
  if (item.isAdvance == 0) {
    let time = '<br /><span style="color:#F56C6C">时间 ' + item.cronExpression.substring(5, 7) + ':' + item.cronExpression.substring(2, 4) + '</span>';
    let week = item.cronExpression.substring(12);
    if (week == '1,2,3,4,5,6,7') {
      result = '每天 ' + time;
    } else {
      let weekArray = week.split(',');
      for (let i = 0; i < weekArray.length; i++) {
        if (weekArray[i] == '1') {
          result = result + '周一、';
        } else if (weekArray[i] == '2') {
          result = result + '周二、';
        } else if (weekArray[i] == '3') {
          result = result + '周三、';
        } else if (weekArray[i] == '4') {
          result = result + '周四、';
        } else if (weekArray[i] == '5') {
          result = result + '周五、';
        } else if (weekArray[i] == '6') {
          result = result + '周六、';
        } else if (weekArray[i] == '7') {
          result = result + '周日、';
        }
      }
      result = result.substring(0, result.length - 1) + ' ' + time;
    }
  } else {
    result = '自定义Cron表达式';
  }
  return result;
}

getList();
</script>
