<template>
  <div style="padding: 6px">
    <!-- 添加或修改通用物模型对话框 -->
    <el-dialog title="编辑参数" :visible.sync="openEdit" width="900px" append-to-body>
      <div style="margin: -30px 0 30px; background-color: #ddd; height: 1px"></div>
      <el-row>
        <el-col :span="12" style="border: 1px solid #ddd; border-radius: 5px; padding: 10px; background-color: #eee">
          <el-form :model="queryParams" :inline="true" label-width="48px" size="small">
            <el-form-item label="" prop="templateName">
              <el-input v-model="queryParams.templateName" placeholder="请输入物模型名称" style="width: 160px" clearable size="mini" @keyup.enter.native="handleQuery" />
            </el-form-item>
            <el-form-item>
              <el-button type="info" icon="el-icon-search" size="mini" @click="handleQuery" style="padding: 5px">搜索</el-button>
            </el-form-item>
            <el-form-item>
              <el-link :underline="false" icon="el-icon-info" type="primary" style="margin-left: 20px">单击应用模板</el-link>
            </el-form-item>
          </el-form>

          <el-table v-loading="loading" :data="templateList" size="mini" @row-click="rowClick" highlight-current-row :border="false" :show-header="false" :row-style="{ backgroundColor: '#eee' }">
            <el-table-column label="选择" width="30" align="center">
              <template slot-scope="scope">
                <input type="radio" :checked="scope.row.isSelect" :disabled="scope.row.datatype == 'array' || scope.row.datatype == 'object'" name="template" />
              </template>
            </el-table-column>
            <el-table-column label="名称" align="left" prop="templateName" />
            <el-table-column label="标识符" align="left" prop="identifier" />
            <el-table-column label="数据类型" align="center" prop="datatype" width="60">
              <template slot-scope="scope">
                <dict-tag :options="dict.type.iot_data_type" :value="scope.row.datatype" />
              </template>
            </el-table-column>
          </el-table>

          <pagination
            v-show="total > 0"
            small
            style="margin: 0 0 10px; background-color: #eee"
            layout="prev, pager, next"
            :total="total"
            :page.sync="queryParams.pageNum"
            :limit.sync="queryParams.pageSize"
            @pagination="getList"
          />
        </el-col>

        <el-col :span="11" :offset="1">
          <el-form ref="form" :model="form" :rules="rules" label-width="80px">
            <el-form-item label="参数名称" prop="name">
              <el-input v-model="form.name" placeholder="例如：温度" style="width: 270px" size="small" />
            </el-form-item>
            <el-form-item label="参数标识" prop="id">
              <el-input v-model="form.id" placeholder="例如：temperature" style="width: 270px" size="small"></el-input>
            </el-form-item>
            <el-form-item label="参数排序" prop="order">
              <el-input-number controls-position="right" v-model="form.order" placeholder="请输入排序" type="number" style="width: 270px" size="small" />
            </el-form-item>

            <el-form-item label="参数特性" prop="property">
              <el-checkbox name="isChart" label="图表展示" @change="isChartChange" v-model="form.isChart" :true-label="1" :false-label="0"></el-checkbox>
              <el-checkbox name="isMonitor" label="实时监测" @change="isMonitorChange" v-model="form.isMonitor" :true-label="1" :false-label="0"></el-checkbox>
              <el-checkbox name="isReadonly" label="只读数据" @change="isReadonlyChange" v-model="form.isReadonly" :true-label="1" :false-label="0"></el-checkbox>
              <el-checkbox name="isHistory" label="历史存储" v-model="form.isHistory" :true-label="1" :false-label="0"></el-checkbox>
              <el-checkbox name="isSharePerm" label="分享权限" v-model="form.isSharePerm" :true-label="1" :false-label="0"></el-checkbox>
            </el-form-item>

            <div style="margin-bottom: 20px; background-color: #ddd; height: 1px"></div>
            <el-form-item label="数据类型" prop="datatype">
              <el-select v-model="form.datatype" placeholder="请选择数据类型" style="width: 125px" size="small">
                <el-option key="integer" label="整数" value="integer"></el-option>
                <el-option key="decimal" label="小数" value="decimal"></el-option>
                <el-option key="bool" label="布尔" value="bool" :disabled="form.isChart == 1"></el-option>
                <el-option key="enum" label="枚举" value="enum" :disabled="form.isChart == 1"></el-option>
                <el-option key="string" label="字符串" value="string" :disabled="form.isChart == 1"></el-option>
              </el-select>
            </el-form-item>
            <div v-if="form.datatype == 'integer' || form.datatype == 'decimal'">
              <el-form-item label="取值范围">
                <el-row>
                  <el-col :span="10">
                    <el-input v-model="form.specs.min" placeholder="最小值" type="number" size="small" />
                  </el-col>
                  <el-col :span="4" align="center">到</el-col>
                  <el-col :span="10">
                    <el-input v-model="form.specs.max" placeholder="最大值" type="number" size="small" />
                  </el-col>
                </el-row>
              </el-form-item>
              <el-form-item label="单位">
                <el-input v-model="form.specs.unit" placeholder="例如：℃" style="width: 308px" size="small" />
              </el-form-item>
              <el-form-item label="步长">
                <el-input-number controls-position="right" v-model="form.specs.step" placeholder="例如：1" type="number" style="width: 308px" size="small" />
              </el-form-item>
            </div>
            <div v-if="form.datatype == 'bool'">
              <el-form-item label="布尔值" prop="">
                <el-row style="margin-bottom: 10px">
                  <el-col :span="10">
                    <el-input v-model="form.specs.falseText" placeholder="例如：关闭" size="small" />
                  </el-col>
                  <el-col :span="10" :offset="1">（0 值对应文本）</el-col>
                </el-row>
                <el-row>
                  <el-col :span="10">
                    <el-input v-model="form.specs.trueText" placeholder="例如：打开" size="small" />
                  </el-col>
                  <el-col :span="10" :offset="1">（1 值对应文本）</el-col>
                </el-row>
              </el-form-item>
            </div>
            <div v-if="form.datatype == 'enum'">
              <el-form-item label="展示方式">
                <el-select v-model="form.specs.showWay" placeholder="请选择展示方式" style="width: 175px">
                  <el-option key="select" label="下拉框" value="select"></el-option>
                  <el-option key="button" label="按钮" value="button"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="枚举项" prop="">
                <el-row v-for="(item, index) in form.specs.enumList" :key="'enum' + index" style="margin-bottom: 10px">
                  <el-col :span="8">
                    <el-input v-model="item.value" placeholder="例如：0" size="small" />
                  </el-col>
                  <el-col :span="11" :offset="1">
                    <el-input v-model="item.text" placeholder="例如：中速挡位" size="small" />
                  </el-col>
                  <el-col :span="3" :offset="1" v-if="index != 0"><a style="color: #f56c6c" @click="removeEnumItem(index)">删除</a></el-col>
                </el-row>
                <div>
                  +
                  <a style="color: #409eff" @click="addEnumItem()">添加枚举项</a>
                </div>
              </el-form-item>
            </div>
            <div v-if="form.datatype == 'string'">
              <el-form-item label="最大长度" prop="">
                <el-row>
                  <el-col :span="10">
                    <el-input v-model="form.specs.maxLength" placeholder="例如：1024" type="number" size="small" />
                  </el-col>
                </el-row>
              </el-form-item>
            </div>
          </el-form>
        </el-col>
      </el-row>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="Parameter">
import { listTemplate } from '@/api/iot/template';

const { proxy } = getCurrentInstance();
const { iot_things_type, iot_data_type, iot_yes_no } = proxy.useDict('iot_things_type', 'iot_data_type', 'iot_yes_no');

// 遮罩层
const loading = ref(true);
// 总条数
const total = ref(0);
// 通用物模型表格数据
const templateList = ref([]);
// 是否显示弹出层
const openEdit = ref(false);
// 参数索引
const index = ref(-1);
// 选中的产品编号
const selectProductId = ref(0);

const data = reactive({
  // 查询参数
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
    type: null,
  },
  // 表单参数
  form: {},
  // 表单校验
  rules: {
    name: [
      {
        required: true,
        message: '参数名称不能为空',
        trigger: 'blur',
      },
    ],
    id: [
      {
        required: true,
        message: '参数标识符不能为空',
        trigger: 'blur',
      },
    ],
    order: [
      {
        required: true,
        message: '模型排序不能为空',
        trigger: 'blur',
      },
    ],
    datatype: [
      {
        required: true,
        message: '数据类型不能为空',
        trigger: 'change',
      },
    ],
  }
})

const { queryParams, form, rules } = toRefs(data);

const props = defineProps({
  data: {
    type: Object,
    default: null,
  }
});

watch(props.data, (newVal, oldVal) => {
  index.value = newVal.index;
  if (newVal && newVal.parameter.name && newVal.parameter.name != '') {
    form.value.name = newVal.parameter.name;
    form.value.id = newVal.parameter.id;
    form.value.order = newVal.parameter.order;
    form.value.isChart = newVal.parameter.isChart ? newVal.parameter.isChart : 0;
    form.value.isHistory = newVal.parameter.isHistory ? newVal.parameter.isHistory : 1;
    form.value.isSharePerm = newVal.parameter.isSharePerm ? newVal.parameter.isSharePerm : 0;
    form.value.isMonitor = newVal.parameter.isMonitor ? newVal.parameter.isMonitor : 0;
    form.value.isReadonly = newVal.parameter.isReadonly ? newVal.parameter.isReadonly : 0;
    form.value.specs = newVal.parameter.datatype;
    form.value.datatype = form.value.specs.type;
    if (!form.value.specs.enumList) {
      form.value.specs.enumList = [
        {
          value: '',
          text: '',
        },
      ];
    }
    if (!form.value.specs.arrayType) {
      form.value.specs.arrayType = 'int';
    }
  }
  openEdit.value = true;
  getList();
});

/** 查询通用物模型列表 */
function getList() {
  loading.value = true;
  listTemplate(queryParams.value).then((response) => {
    //模板列表初始化isSelect值，用于单选
    for (let i = 0; i < response.rows.length; i++) {
      response.rows[i].isSelect = false;
    }
    templateList.value = response.rows;
    total.value = response.total;
    setRadioSelected(selectProductId.value);
    loading.value = false;
  });
}
/** 单选数据 */
function rowClick(item) {
  if (item != null && item.datatype != 'array' && item.datatype != 'object') {
    form.value.name = item.templateName;
    form.value.id = item.identifier;
    form.value.order = item.modelOrder;
    form.value.isChart = item.isChart ? item.isChart : 0;
    form.value.isHistory = item.isHistory ? item.isHistory : 1;
    form.value.isSharePerm = item.isSharePerm ? item.isSharePerm : 0;
    form.value.isReadonly = item.isReadonly ? item.isReadonly : 0;
    form.value.isMonitor = item.isMonitor ? item.isMonitor : 0;
    form.value.datatype = item.datatype;
    // Json转对象
    form.value.specs = JSON.parse(item.specs);
    if (!form.value.specs.enumList) {
      form.value.specs.enumList = [
        {
          value: '',
          text: '',
        },
      ];
    }
    if (!form.value.specs.arrayType) {
      form.value.specs.arrayType = 'int';
    }
    setRadioSelected(item.templateId);
  }
}
/** 设置单选按钮选中 */
function setRadioSelected(templateId) {
  for (let i = 0; i < templateList.value.length; i++) {
    if (templateList.value[i].templateId == templateId) {
      templateList.value[i].isSelect = true;
    } else {
      templateList.value[i].isSelect = false;
    }
  }
}
// 取消按钮
function cancel() {
  openEdit.value = false;
  reset();
}
// 表单重置
function reset() {
  index.value = -1;
  form.value = {
    name: null,
    id: null,
    order: 0,
    datatype: 'integer',
    isChart: 0,
    isHistory: 1,
    isSharePerm: 0,
    isMonitor: 0,
    isReadonly: 0,
    specs: {
      enumList: [
        {
          value: '',
          text: '',
        },
      ],
      showWay: 'select', // 显示方式select=下拉选择框，button=按钮
    },
  };
  proxy.resetForm('form');
}
/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}
/** 提交按钮 */
function submitForm() {
  proxy.$refs['form'].validate((valid) => {
    if (valid) {
      // 格式化datatype
      form.value.datatype = formatThingsSpecs();
      // 清空不需要存储数据
      delete form.value.specs;
      openEdit.value = false;
      // 返回参数对象
      let data = {
        parameter: JSON.parse(JSON.stringify(form.value)),
        index: this.index,
      };
      console.log('data', data);
      proxy.$emit('dataEvent', data);
      reset();
    }
  });
}
// 是否图表展示改变
function isChartChange() {
  if (form.value.isChart == 1) {
    form.value.isReadonly = 1;
  } else {
    form.value.isMonitor = 0;
  }
}
// 是否实时监测改变
function isMonitorChange() {
  if (form.value.isMonitor == 1) {
    form.value.isReadonly = 1;
    form.value.isChart = 1;
  }
}
// 是否只读数据改变
function isReadonlyChange() {
  if (form.value.isReadonly == 0) {
    form.value.isMonitor = 0;
    form.value.isChart = 0;
  }
}
// 格式化物模型
function formatThingsSpecs() {
  var data = {};
  data.type = form.value.datatype;
  if (form.value.datatype == 'integer' || form.value.datatype == 'decimal') {
    data.min = Number(form.value.specs.min ? form.value.specs.min : 0);
    data.max = Number(form.value.specs.max ? form.value.specs.max : 100);
    data.unit = form.value.specs.unit ? form.value.specs.unit : '';
    data.step = Number(form.value.specs.step ? form.value.specs.step : 1);
  } else if (form.value.datatype == 'string') {
    data.maxLength = Number(form.value.specs.maxLength ? form.value.specs.maxLength : 1024);
  } else if (form.value.datatype == 'bool') {
    data.falseText = form.value.specs.falseText ? form.value.specs.falseText : '关闭';
    data.trueText = form.value.specs.trueText ? form.value.specs.trueText : '打开';
  } else if (form.value.datatype == 'array') {
    data.arrayType = form.value.specs.arrayType;
  } else if (form.value.datatype == 'enum') {
    data.showWay = form.value.specs.showWay;
    if (form.value.specs.enumList && form.value.specs.enumList[0].text != '') {
      data.enumList = form.value.specs.enumList;
    } else {
      data.showWay = 'select';
      data.enumList = [
        {
          value: '0',
          text: '低',
        },
        {
          value: '1',
          text: '高',
        },
      ];
    }
  }
  return data;
}
/** 添加枚举项 */
function addEnumItem() {
  form.value.specs.enumList.push({
    value: '',
    text: '',
  });
}
/** 删除枚举项 */
function removeEnumItem(index) {
  form.value.specs.enumList.splice(index, 1);
}

getList();
reset();
</script>

<style>
  .specsColor {
    background-color: #fcfcfc;
  }
</style>