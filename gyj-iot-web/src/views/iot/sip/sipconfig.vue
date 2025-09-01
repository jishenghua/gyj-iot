<template>
  <div style="padding:6px;">
    <el-form ref="form" :model="form" label-width="100px">
      <el-row :gutter="100">
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="8">
          <el-form-item label="默认配置" prop="isdefault">
            <el-switch v-model="form.isdefault" :active-value="1" :inactive-value="0"/>
          </el-form-item>
          <el-form-item label="服务器地址" prop="ip">
            <el-input v-model="form.ip" disabled />
          </el-form-item>
          <el-form-item label="服务器域" prop="domain">
            <el-input v-model="form.domain" />
          </el-form-item>
          <el-form-item label="认证密码" prop="password">
            <el-input v-model="form.password" placeholder="请输入认证密码" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="8">
          <el-form-item label="接入方式">
            <el-input v-model="accessWay" disabled>
            </el-input>
          </el-form-item>
          <el-form-item label="服务器端口" prop="port">
            <el-input v-model="form.port" type="number" disabled />
          </el-form-item>
          <el-form-item label="服务器ID" prop="serverSipid">
            <el-input v-model="form.serverSipid" />
          </el-form-item>
        </el-col>
        <el-col :xs="23" :sm="23" :md="23" :lg="23" :xl="15">
          <el-form-item style="text-align:center;margin-top:20px;">
            <el-button v-show="form.id && productInfo.status != 2" v-hasPermi="['iot:video:edit']" type="primary"
              @click="submitForm">修 改</el-button>
            <el-button v-show="!form.id && productInfo.status != 2" v-hasPermi="['iot:video:add']" type="primary"
              @click="submitForm">新 增</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>



<script setup name="ConfigSip">
import {
  getSipconfig,
  addSipconfig,
  updateSipconfig,
} from '@/api/iot/sipConfig';

const { proxy } = getCurrentInstance();

// 接入方式
const accessWay = ref('国标GB28181');
// 遮罩层
const loading = ref(true);
// 选中数组
const ids = ref([]);
// 总条数
const total = ref(0);
// sip系统配置表格数据
const sipconfigList = ref([]);
// 弹出层标题
const title = ref('');
// 是否显示弹出层
const open = ref(false);

const data = reactive({
  // 查询参数
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    productId: null,
  },
  // 表单参数
  form: {},
  // 表单校验
  rules: {
    domain: [{
      required: true,
      message: '服务器域不能为空',
      trigger: 'blur',
    }],
    serverSipid: [{
      required: true,
      message: '服务器sipid不能为空',
      trigger: 'blur',
    }],
    password: [{
      required: true,
      message: 'sip认证密码不能为空',
      trigger: 'blur',
    }],
  }
})

const { queryParams, form, rules } = toRefs(data);

const props = defineProps({
  product: {
    type: Object,
    default: null,
  },
});

// 获取到父组件传递的productId后，刷新列表
watch(props.product, (newVal, oldVal) => {
  productInfo.value = newVal;
  if (productInfo.value && productInfo.value.productId != 0) {
    // 表单没有数据则获取默认配置
    if (!form.value.id) {
      getSipconfigInfo(true);
    }
  }
});

/** 获取产品下第一条SIP配置 */
function getSipconfigInfo(isDefault) {
  getSipconfig(productInfo.value.productId, isDefault).then(response => {
    form.value = response.data;
    if (isDefault) {
      submitForm();
    }
  });
}
/** 提交按钮 */
function submitForm() {
  proxy.$refs['form'].validate(valid => {
    if (valid) {
      form.value.productId = product.value.productId;
      if (form.value.isdefault == null) {
        form.value.isdefault = 0;
      }
      if (form.value.id != null) {
        updateSipconfig(form.value).then(response => {
          proxy.$modal.msgSuccess('修改成功');
        });
      } else {
        addSipconfig(form.value).then(response => {
          proxy.$modal.msgSuccess('新增成功');
          getSipconfigInfo(false);
        });
      }
    }
  });
}

productInfo.value = product.value;
if (productInfo.value && productInfo.value.productId != 0) {
  getSipconfigInfo(false);
}
</script>

<style>
  .specsColor {
    background-color: #fcfcfc;
  }
</style>