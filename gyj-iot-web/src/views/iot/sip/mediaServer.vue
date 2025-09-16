<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="75px">
      <el-form-item>
        <el-button type="primary" plain icon="Plus" @click="add"
                   v-hasPermi="['iot:video:add']">新增节点</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="warning" plain icon="Refresh" @click="getServerList">刷新</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="30">
      <el-col :xs="24" :sm="12" :md="12" :lg="8" :xl="6" v-for="(item, index) in mediaServerList" :key="index"
              style="margin-bottom: 30px; text-align: center">
        <el-card shadow="always" class="card-item">
          <el-row :gutter="10">
            <el-col :span="15">
              <el-descriptions :column="1" style="white-space: nowrap">
                <el-descriptions-item label="配置名称">
                  {{ item.serverId }}
                </el-descriptions-item>
                <el-descriptions-item label="服务器IP">
                  {{ item.ip }}
                </el-descriptions-item>
                <el-descriptions-item label="播放协议">
                  {{ item.protocol }}
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">
                  {{ parseTime(item.createTime, '{y}-{m}-{d}') }}
                </el-descriptions-item>
              </el-descriptions>
            </el-col>
            <el-col :span="8">
              <div style="margin-top: 10px">
                <el-image :src="zlmLogo"></el-image>
              </div>
            </el-col>
          </el-row>
          <el-button-group style="margin-top: 10px">
            <el-button type="danger" style="padding: 5px 10px" icon="Delete"
                       v-hasPermi="['iot:video:remove']" @click="del(item)">删除</el-button>
            <el-button type="primary" style="padding: 5px 15px" icon="View" @click="view(item)"
                       v-hasPermi="['iot:video:query']">查看</el-button>
            <el-button v-if="!istrue" type="success" style="padding: 5px 15px" icon="Odometer"
                       @click.native.prevent="edit(item)" v-hasPermi="['iot:video:edit']">编辑
            </el-button>
            <el-button v-else type="success" style="padding: 5px 15px" icon="Odometer"
                       :loading="true" disabled>重启中...
            </el-button>
          </el-button-group>
        </el-card>
      </el-col>
    </el-row>
    <el-empty description="暂无数据，请添加流媒体服务器节点" v-if="total == 0"></el-empty>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
                :pageSizes="[12, 24, 36, 60]" @pagination="getServerList" />

    <mediaServerEdit ref="mediaServerEditRef" @show="getServerList" :edit-flag="editFlag"></mediaServerEdit>
  </div>
</template>

<script setup name="MediaServer">
import mediaServerEdit from '@/views/iot/sip/mediaServer-edit.vue';
import { delmediaServer, listmediaServer } from '@/api/iot/mediaServer';
import zlmLogo from '@/assets/images/zlm-logo.png'

const { proxy } = getCurrentInstance();

// 遮罩层
const loading = ref(true);
const editFlag = ref(false);
const istrue = ref(false);
//设备列表
const mediaServerList = ref([]);
const winHeight = ref(window.innerHeight - 200);
const updateLooper = ref(false);
const currentPage = ref(1);
const count = ref(15);
const num = ref(getNumberByWidth());
const total = ref(0);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
  },
})

const { queryParams } = toRefs(data);

onMounted(() => {
  initData();
});

onUnmounted(() => {
  clearTimeout(updateLooper);
});

function initData() {
  getServerList();
}
function pageNumChange(val) {
  queryParams.value.pageNum = val;
  getServerList();
}
function pageSizeChange(val) {
  queryParams.value.pageSize = val;
  getServerList();
}
function getServerList() {
  loading.value = true;
  listmediaServer(queryParams.value).then((response) => {
    mediaServerList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}
function add() {
  proxy.$refs['mediaServerEditRef'].openDialog(null, initData.value, editFlag.value);
  editFlag.value = true;
}
function view(row) {
  proxy.$refs['mediaServerEditRef'].openDialog(row, initData.value, editFlag.value);
  editFlag.value = false;
}
function edit(row) {
  proxy.$refs['mediaServerEditRef'].openDialog(row, initData.value, editFlag.value);
  editFlag.value = true;
}
function delay() {
  let n = 5;
  // 定义定时器time
  const time = setInterval(() => {
    istrue.value = true;
    n--;
    // 如果n<0，清除定时器，禁用状态取消，文字提示为空（不显示）
    if (n < 0) {
      istrue.value = false;
      clearInterval(time);
    }
  }, 1000);
}
function del(row) {
  const ids = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除流媒体服务器配置编号为"' + ids + '"的数据项？')
  .then(function () {
    delmediaServer(ids).then(() => {
      getServerList();
      proxy.$modal.msgSuccess('删除成功');
    })
  })
}
function getNumberByWidth() {
  let candidateNums = [1, 2, 3, 4, 6, 8, 12, 24];
  let clientWidth = window.innerWidth - 30;
  let interval = 20;
  let itemWidth = 360;
  let num = (clientWidth + interval) / (itemWidth + interval);
  let result = Math.ceil(24 / num);
  let resultVal = 24;
  for (let i = 0; i < candidateNums.length; i++) {
    let value = candidateNums[i];
    if (i + 1 >= candidateNums.length) {
      return 24;
    }
    if (value <= result && candidateNums[i + 1] > result) {
      return value;
    }
  }
  return resultVal;
}
</script>

<style scoped>
.card-item {
  border-radius: 10px;
  padding: 15px 0px;
}
</style>
