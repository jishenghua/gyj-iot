<template>
  <div style="padding-left: 20px">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-refresh" size="mini" @click="getList">刷新</el-button>
      </el-col>
    </el-row>
    <el-table v-loading="loading" :data="channelList" size="mini">
      <el-table-column label="设备ID" align="center" prop="deviceSipId" />
      <el-table-column label="通道ID" align="center" prop="channelSipId" />
      <el-table-column label="快照" min-width="120">
        <template v-slot:default="scope">
          <el-image v-if="isVideoChannel(scope.row)" :src="getSnap(scope.row)" :preview-src-list="getBigSnap(scope.row)" :fit="'contain'" style="width: 60px">
            <div slot="error" class="image-slot">
              <i class="el-icon-picture-outline"></i>
            </div>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column label="通道名称" align="center" prop="channelName" />
      <el-table-column label="产品型号" align="center" prop="model" />
      <el-table-column label="推流状态" align="center" prop="streamPush" >
        <template #default="scope">
          <el-tag type="warning" v-if="scope.row.streamPush === 0">无</el-tag>
          <el-tag type="success" v-if="scope.row.streamPush === 1">推流中</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="直播录像状态" align="center" prop="streamRecord" >
        <template #default="scope">
          <el-tag type="warning" v-if="scope.row.streamRecord === 0">无</el-tag>
          <el-tag type="success" v-if="scope.row.streamRecord === 1">录像中</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="录像转存状态" align="center" prop="videoRecord" >
        <template #default="scope">
          <el-tag type="warning" v-if="scope.row.videoRecord === 0">无</el-tag>
          <el-tag type="success" v-if="scope.row.videoRecord === 1">转存中</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <dict-tag :options="sip_gen_status" :value="scope.row.status" size="mini" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="120" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button size="small" type="success" icon="el-icon-video-play" style="padding: 5px" :disabled="scope.row.status !== 2" @click="sendDevicePush(scope.row)">查看直播</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="Channel">
import { listChannel, getChannel, delChannel } from '@/api/iot/channel';

const { proxy } = getCurrentInstance();
const { sip_gen_status, video_type, channel_type } = proxy.useDict('sip_gen_status', 'video_type', 'channel_type');

const loadSnap = ref({});
// 设备信息
const deviceInfo = ref({});
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
// 监控设备通道信息表格数据
const channelList = ref([]);
// 弹出层标题
const title = ref('');
// 是否显示弹出层
const open = ref(false);

const data = reactive({
  // 查询参数
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    deviceSipId: null,
  },
  // 表单参数
  form: {},
})

const { queryParams, form } = toRefs(data);

const props = defineProps({
  device: {
    type: Object,
    default: null,
  }
});

// 获取到父组件传递的productId后，刷新列表
watch(props.device, (newVal, oldVal) => {
  deviceInfo.value = newVal;
  if (deviceInfo.value && deviceInfo.value.deviceId != 0) {
    queryParams.value.deviceSipId = deviceInfo.value.serialNumber;
  }
});

//通知设备上传媒体流
function sendDevicePush(itemData) {
  var data = { tabName: 'videoLive', channelId: itemData.channelSipId };
  proxy.$emit('playerEvent', data);
  console.log('通知设备推流：' + itemData.deviceSipId + ' : ' + itemData.channelSipId);
}
/** 查询监控设备通道信息列表 */
function getList() {
  loading.value = true;
  listChannel(this.queryParams).then((response) => {
    console.log(response);
    channelList.value = response.rows;
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
    channelId: null,
    channelSipId: null,
    deviceSipId: null,
    channelName: null,
    manufacture: null,
    model: null,
    owner: null,
    civilcode: null,
    block: null,
    address: null,
    parentid: null,
    ipaddress: null,
    port: null,
    password: null,
    ptztype: null,
    ptztypetext: null,
    status: 0,
    longitude: null,
    latitude: null,
    streamid: null,
    subcount: null,
    parental: 1,
    hasaudio: 1,
  };
  proxy.resetForm('form');
}
/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}
/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const channelId = row.channelId || ids.value;
  getChannel(channelId).then((response) => {
    form.value = response.data;
    open.value = true;
    title.value = '修改监控设备通道信息';
  });
}
/** 删除按钮操作 */
function handleDelete(row) {
  const channelIds = row.channelId || ids.value;
  proxy.$modal.confirm('是否确认删除监控设备通道信息编号为"' + channelIds + '"的数据项？')
    .then(function () {
      return delChannel(channelIds);
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess('删除成功');
    })
    .catch(() => {});
}
function getSnap (row) {
  console.log('getSnap:' + import.meta.env.VITE_APP_BASE_API + '/profile/snap/' + row.deviceSipId + '_' + row.channelSipId + '.jpg');
  return import.meta.env.VITE_APP_BASE_API + '/profile/snap/' + row.deviceSipId + '_' + row.channelSipId + '.jpg';
}
function getBigSnap (row) {
  return [getSnap(row)];
}
function isVideoChannel (row) {
  let channelType = row.channelSipId.substring(10, 13);
  // 111-DVR编码;112-视频服务器编码;118-网络视频录像机(NVR)编码;131-摄像机编码;132-网络摄像机(IPC)编码
  return !(channelType !== '111' && channelType !== '112' && channelType !== '118' && channelType !== '131' && channelType !== '132');
}

queryParams.value.deviceSipId = proxy.device.serialNumber;
getList();
</script>
