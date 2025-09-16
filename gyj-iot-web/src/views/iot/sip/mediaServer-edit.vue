<template>
  <div id="mediaServerEdit" v-loading="isLoging">
    <el-dialog title="流媒体服务器节点" :width="dialogWidth" top="2rem" :close-on-click-modal="false" v-model="showDialog" :destroy-on-close="true" @close="close()">
      <div id="formStep" style="margin-top: 1rem; margin-right: 20px">
        <el-form v-if="currentStep == 1" ref="mediaServerRef" :rules="rules" :model="mediaServerForm" label-width="280px" style="width: 70%">
<!--          <el-form-item label="所属租户" prop="productName">-->
<!--            <el-input readonly v-model="mediaServerForm.tenantName" placeholder="请选择所属租户">-->
<!--              <el-button slot="append" @click="selectUser()">选择</el-button>-->
<!--            </el-input>-->
<!--          </el-form-item>-->
          <el-form-item label="服务器IP" prop="ip">
            <el-input v-model="mediaServerForm.ip" placeholder="媒体服务IP" clearable></el-input>
          </el-form-item>
          <el-form-item label="Http端口" prop="portHttp">
            <el-input v-model="mediaServerForm.portHttp" placeholder="媒体服务HTTP端口" clearable></el-input>
          </el-form-item>
          <el-form-item label="服务密钥" prop="secret">
            <el-input v-model="mediaServerForm.secret" placeholder="媒体服务SECRET" clearable></el-input>
          </el-form-item>
          <el-form-item>
            <div style="float: right; font-size: 28px">
              <el-button @click="close">取消</el-button>
              <el-button type="success" @click="checkServer" :loading="btnLoading">测试</el-button>
              <el-button type="primary" v-if="currentStep === 1 && serverCheck === 1" @click="next">下一步</el-button>
            </div>
          </el-form-item>
        </el-form>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form v-if="currentStep === 2 || currentStep === 3" ref="mediaServerRef1" :rules="rules" :model="mediaServerForm" label-width="140px" :disabled="!editFlag">
              <el-form-item label="配置名称" prop="serverId">
                <el-input v-model="mediaServerForm.serverId" placeholder="配置名称" clearable></el-input>
              </el-form-item>
              <el-form-item label="服务器IP" prop="ip">
                <el-input v-if="currentStep === 2" v-model="mediaServerForm.ip" disabled></el-input>
                <el-input v-if="currentStep === 3" v-model="mediaServerForm.ip"></el-input>
              </el-form-item>
              <el-form-item label="播放协议" prop="protocol">
                <el-select v-model="mediaServerForm.protocol" style="width: 100%">
                  <el-option key="http" label="http" value="http"></el-option>
                  <el-option key="https" label="https" value="https"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="HookUrl" prop="hookurl">
                <el-input v-model="mediaServerForm.hookurl" placeholder="HookUrl" clearable></el-input>
              </el-form-item>
              <el-form-item label="Http端口" prop="portHttp">
                <el-input v-if="currentStep === 2" v-model="mediaServerForm.portHttp" disabled></el-input>
                <el-input v-if="currentStep === 3" v-model="mediaServerForm.portHttp"></el-input>
              </el-form-item>
              <el-form-item label="Https端口" prop="portHttps">
                <el-input v-model="mediaServerForm.portHttps" placeholder="Https端口" clearable></el-input>
              </el-form-item>
              <el-form-item label="Rtsp端口" prop="portRtsp">
                <el-input v-model="mediaServerForm.portRtsp" placeholder="Rtsp端口" clearable></el-input>
              </el-form-item>
            </el-form>
          </el-col>
          <el-col :span="12">
            <el-form v-if="currentStep === 2 || currentStep === 3" ref="mediaServerForm2" :rules="rules" :model="mediaServerForm" label-width="180px" :disabled="!editFlag">
              <el-form-item label="流媒体密钥" prop="secret">
                <el-input v-if="currentStep === 2" v-model="mediaServerForm.secret" disabled></el-input>
                <el-input v-if="currentStep === 3" v-model="mediaServerForm.secret"></el-input>
              </el-form-item>
              <el-form-item label="服务器域名" prop="domain">
                <el-input v-model="mediaServerForm.domain" placeholder="服务器域名" clearable></el-input>
              </el-form-item>
              <el-form-item label="自动配置">
                <el-switch v-model="mediaServerForm.autoConfig"></el-switch>
              </el-form-item>
              <el-form-item label="收流模式">
                <el-switch active-text="多端口" inactive-text="单端口" @change="portRangeChange" v-model="mediaServerForm.rtpEnable"></el-switch>
              </el-form-item>
              <el-form-item v-if="!mediaServerForm.rtpEnable" label="收流端口" prop="rtpProxyPort">
                <el-input v-model.number="mediaServerForm.rtpProxyPort" clearable></el-input>
              </el-form-item>
              <el-form-item v-if="mediaServerForm.rtpEnable" label="收流端口">
                <el-input v-model="rtpPortRange1" placeholder="起始" @change="portRangeChange" clearable style="width: 100px" prop="rtpPortRange1"></el-input>
                <el-input v-model="rtpPortRange2" placeholder="终止" @change="portRangeChange" clearable style="width: 100px" prop="rtpPortRange2" ></el-input>
              </el-form-item>
              <el-form-item label="Rtmp端口" prop="portRtmp">
                <el-input v-model="mediaServerForm.portRtmp" placeholder="Rtmp端口" clearable></el-input>
              </el-form-item>
              <el-form-item label="录像管理端口" prop="recordPort">
                <el-input v-model.number="mediaServerForm.recordPort" :disabled="!editFlag">
                  <el-button v-if="mediaServerForm.recordPort > 0" class="el-icon-check" slot="append" type="primary" @click="checkRecordServer"></el-button>
                </el-input>
                <i v-if="recordServerCheck === 1" class="el-icon-success" style="color: #3caf36; position: absolute; top: 14px"></i>
                <i v-if="recordServerCheck === 2" class="el-icon-loading" style="color: #3caf36; position: absolute; top: 14px"></i>
                <i v-if="recordServerCheck === -1" class="el-icon-error" style="color: #c80000; position: absolute; top: 14px"></i>
              </el-form-item>
              <el-form-item>
                <div style="float: right">
                  <el-button type="primary" @click="onSubmit"  v-if="editFlag">提交</el-button>
                  <el-button @click="close"  v-if="editFlag">关闭</el-button>
                </div>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
    <!-- 选择用户 -->
    <user-list ref="userList" @userEvent="getUserData($event)" />
  </div>
</template>

<script setup name="MediaServerEdit">
import { addmediaServer, checkmediaServer, updatemediaServer } from '@/api/iot/mediaServer';
import userList from '@/views/iot/sip/user-list.vue';
import { inject } from 'vue';

const { proxy } = getCurrentInstance();

const isValidIp = (rule, value, callback) => {
  // 校验IP是否符合规则
  var reg = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/;
  if (!reg.test(value)) {
    return callback(new Error('请输入有效的IP地址'));
  } else {
    callback();
  }
  return true;
};
const isValidPort = (rule, value, callback) => {
  // 校验IP是否符合规则
  var reg = /^(([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-5]{2}[0-3][0-5]))$/;
  if (!reg.test(value)) {
    return callback(new Error('请输入有效的端口号'));
  } else {
    callback();
  }
  return true;
};

const props = defineProps({
  editFlag: {
    type: Boolean,
    default: false,
  }
});

const tempTenantId = ref('');
const tempTenantName = ref('');
const btnLoading = ref(false);
const dialogWidth = ref('');
const defaultWidth = ref(1000);
const listChangeCallback = ref(null);
const showDialog = ref(false);
const isLoging = ref(false);
const dialogLoading = ref(false);
const currentStep = ref(1);
const platformList = ref([]);
const serverCheck = ref(0);
const recordServerCheck = ref(0);
const mediaServerForm = ref({
  serverId: '',
  ip: '',
  domain: '',
  productId: '',
  productName: '',
  tenantId: '',
  tenantName: '',
  autoConfig: true,
  hookurl: '',
  secret: '',
  portHttp: '',
  portHttps: '',
  recordPort: '',
  portRtmp: '',
  portRtsp: '',
  rtpEnable: true,
  rtpPortRange: '',
  rtpProxyPort: '',
});
const rtpPortRange1 = ref(30000);
const rtpPortRange2 = ref(30100);

const data = reactive({
  rules: {
    ip: [{ required: true, validator: isValidIp, message: '请输入有效的IP地址', trigger: 'blur' }],
    portHttp: [{ required: true, validator: isValidPort, message: '请输入有效的端口号', trigger: 'blur' }],
    portHttps: [{ required: true, validator: isValidPort, message: '请输入有效的端口号', trigger: 'blur' }],
    recordPort: [{ required: true, validator: isValidPort, message: '请输入有效的端口号', trigger: 'blur' }],
    portRtmp: [{ required: true, validator: isValidPort, message: '请输入有效的端口号', trigger: 'blur' }],
    portRtsp: [{ required: true, validator: isValidPort, message: '请输入有效的端口号', trigger: 'blur' }],
    rtpPortRange1: [{ required: true, validator: isValidPort, message: '请输入有效的端口号', trigger: 'blur' }],
    rtpPortRange2: [{ required: true, validator: isValidPort, message: '请输入有效的端口号', trigger: 'blur' }],
    rtpProxyPort: [{ required: true, validator: isValidPort, message: '请输入有效的端口号', trigger: 'blur' }],
    secret: [{ required: true, message: '请输入secret', trigger: 'blur' }],
  }
})

const { rules } = toRefs(data);

function setDialogWidth() {
  let val = document.body.clientWidth;
  if (val < defaultWidth.value) {
    dialogWidth.value = '100%';
  } else {
    dialogWidth.value = defaultWidth.value + 'px';
  }
}
function openDialog(param, callback) {
  showDialog.value = true;
  listChangeCallback.value = callback;
  if (param != null) {
    if (param.autoConfig === 1) {
      param.autoConfig = param.autoConfig === 1;
    } else if (param.autoConfig === 0) {
      param.autoConfig = param.autoConfig === 1;
    }

    if (param.rtpEnable === 1) {
      param.rtpEnable = param.rtpEnable === 1;
    } else if (param.rtpEnable === 0) {
      param.rtpEnable = param.rtpEnable === 1;
    }

    mediaServerForm.value = param;
    currentStep.value = 3;
    if (param.rtpPortRange) {
      let rtpPortRange = mediaServerForm.value.rtpPortRange.split(',');
      if (rtpPortRange.length > 0) {
        rtpPortRange1.value = rtpPortRange[0];
        rtpPortRange2.value = rtpPortRange[1];
      }
    }
  }
}
function checkServer() {
  proxy.$refs["mediaServerRef"].validate((valid) => {
    if (valid) {
      btnLoading.value = true;
      serverCheck.value = 0;
      let query = {
        ip: mediaServerForm.value.ip,
        port: mediaServerForm.value.portHttp,
        secret: mediaServerForm.value.secret,
      };
      checkmediaServer(query).then((response) => {
        btnLoading.value = false;
        if (response.data != null) {
          mediaServerForm.value = response.data;
          mediaServerForm.value.autoConfig = true;
          mediaServerForm.value.rtpEnable = true;
          mediaServerForm.value.protocol = 'http';
          mediaServerForm.value.domain = 'gyjiot.com';
          mediaServerForm.value.enabled = 1;
          mediaServerForm.value.tenantId = tempTenantId.value;
          mediaServerForm.value.tenantName = tempTenantName.value;
          mediaServerForm.value.serverId = 'gyjiot';
          mediaServerForm.value.hookurl = 'java:8080';
          mediaServerForm.value.portHttps = 8443;
          mediaServerForm.value.recordPort = 18081;
          mediaServerForm.value.portRtmp = 1935;
          mediaServerForm.value.portRtsp = 554;
          mediaServerForm.value.rtpProxyPort = '';
          rtpPortRange1.value = 30000;
          rtpPortRange2.value = 30100;
          serverCheck.value = 1;
          proxy.$modal.alertSuccess('配置地址连接成功');
        } else {
          serverCheck.value = -1;
          proxy.$modal.alertError('配置地址无法连接');
        }
      })
      .catch((error) => {
        serverCheck.value = -1;
        proxy.$message({
          showClose: true,
          message: error,
          type: 'error',
        });
      });
    }
  });
}
function next() {
  currentStep.value = 2;
  defaultWidth.value = 900;
  setDialogWidth();
}
function checkRecordServer() {
  recordServerCheck.value = 2;
  if (mediaServerForm.value.recordPort <= 0 || mediaServerForm.value.recordPort > 65535) {
    recordServerCheck.value = -1;
    proxy.$message({
      showClose: true,
      message: '端口号应该在-65535之间',
      type: 'error',
    });
    return;
  }
}
function onSubmit() {
  dialogLoading.value = true;
  mediaServerForm.value.rtpEnable = mediaServerForm.value.rtpEnable ? 1 : 0;
  mediaServerForm.value.autoConfig = mediaServerForm.value.autoConfig ? 1 : 0;
  if (mediaServerForm.value.id != null) {
    updatemediaServer(mediaServerForm.value).then((response) => {
      proxy.$modal.msgSuccess('修改成功');
      showDialog.value = false;
      proxy.$emit('show');
    });
  } else {
    portRangeChange();
    addmediaServer(mediaServerForm.value).then((response) => {
      proxy.$modal.msgSuccess('新增成功');
      showDialog.value = false;
      proxy.$emit('show');
    });
  }
}
function close() {
  showDialog.value = false;
  dialogLoading.value = false;
  mediaServerForm.value = {
    serverId: '',
    ip: '',
    domain: '',
    autoConfig: true,
    hookurl: '',
    secret: '',
    portHttp: '',
    portHttps: '',
    recordPort: '',
    portRtmp: '',
    portRtsp: '',
    rtpEnable: true,
    rtpPortRange: '',
    rtpProxyPort: '',
  };
  rtpPortRange1.value = 30000;
  rtpPortRange2.value = 30100;
  listChangeCallback.value = null;
  currentStep.value = 1;
}
function portRangeChange() {
  if (mediaServerForm.value.rtpEnable) {
    mediaServerForm.value.rtpPortRange = rtpPortRange1.value + ',' + rtpPortRange2.value;
    console.log(mediaServerForm.value.rtpPortRange);
  }
}
function selectUser() {
  proxy.$refs.userList.open = true;
  proxy.$refs.userList.getList();
}
function getUserData(user) {
  tempTenantId.value = user.userId;
  tempTenantName.value = user.userName;
  mediaServerForm.value.tenantId = user.userId;
  mediaServerForm.value.tenantName = user.userName;
}

setDialogWidth()
defineExpose({
  openDialog
});
</script>
