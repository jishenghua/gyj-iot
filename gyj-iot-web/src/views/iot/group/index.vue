<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="分组名称" prop="groupName">
        <el-input v-model="queryParams.groupName" placeholder="请输入分组名称" clearable
                  @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item v-if="isAdmin" label="我的分组">
        <el-switch v-model="myGroup" @change="myGroupChange"></el-switch>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
      <el-form-item style="float: right">
        <el-button v-hasPermi="['iot:group:add']" type="primary" plain icon="Plus"
                   @click="handleAdd">新增</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="groupList" border @selection-change="handleSelectionChange">
      <el-table-column label="分组名称" align="center" prop="groupName" width="200" />
      <el-table-column label="分组排序" align="center" prop="groupOrder" width="100" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column v-if="isAdmin" label="所属用户" align="center" prop="userName" width="100" />
      <el-table-column label="备注" align="left" header-align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="320">
        <template #default="scope">
          <el-button v-hasPermi="['iot:device:query']" size="small" style="padding: 5px"
                     icon="Search" @click="handleViewDevice(scope.row.groupId)">查看设备</el-button>
          <el-button v-hasPermi="['iot:group:add']" size="small" style="padding: 5px" icon="FolderAdd"
                     @click="selectDevice(scope.row)">添加设备</el-button>
          <el-button v-hasPermi="['iot:group:edit']" size="small" style="padding: 5px"
                     icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button v-hasPermi="['iot:group:remove']" size="small" style="padding: 5px"
                     icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
                @pagination="getList" />

    <!-- 分组设备列表 -->
    <device-list ref="groupDeviceList"></device-list>

    <!-- 添加或修改设备分组对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="groupRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="分组名称" prop="groupName">
          <el-input v-model="form.groupName" placeholder="请输入分组名称" />
        </el-form-item>
        <el-form-item label="分组排序" prop="groupOrder">
          <el-input v-model="form.groupOrder" type="number" placeholder="请输入分组排序" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Group">
import deviceList from './device-list'
import { listGroup, getGroup, delGroup, addGroup, updateGroup } from '@/api/iot/group'
import {getUserId} from "@/utils/auth";
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()
const { proxy } = getCurrentInstance();

// 是否管理员
const isAdmin = ref(false);
// 我的分组
const myGroup = ref(false);
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
// 设备分组表格数据
const groupList = ref([]);
// 弹出层标题
const title = ref('');
// 是否显示弹出层
const open = ref(false);
// 设备分组
const group = ref({});

const data = reactive({
  // 表单参数
  form: {},
  // 查询参数
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    groupName: null,
    userId: null,
  },
  // 表单校验
  rules: {
    groupName: [
      {
        required: true,
        message: '分组名称不能为空',
        trigger: 'blur',
      },
      {
        min: 1,
        max: 64,
        message: '分组名称不能少于1个字符和超过64字符',
      },
    ],
    groupOrder: [
      {
        required: true,
        message: '分组排序不能为空,最大值为99',
        trigger: 'blur',
      },
      {
        max: 128,
        message: '分组排序不能少于1个位和超过10位',
        trigger: 'blur',
      },
    ],
  }
})

const { queryParams, form, rules } = toRefs(data);

function init() {
  if (userStore.roles.indexOf("tenant") === -1 && userStore.roles.indexOf('general') === -1) {
    isAdmin.value = true
  }
}
// 我的分组改变事件
function myGroupChange() {
  queryParams.value.userId = myGroup.value ? getUserId() : null;
  handleQuery();
}
/** 查看设备按钮操作 */
function handleViewDevice(groupId) {
  proxy.$router.push({
    path: '/iot/device',
    query: {
      t: Date.now(),
      groupId: groupId,
    },
  })
}
/** 查询设备分组列表 */
function getList() {
  loading.value = true
  listGroup(queryParams.value).then((response) => {
    groupList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}
// 取消按钮
function cancel() {
  open.value = false
  reset()
}
// 表单重置
function reset() {
  form.value = {
    groupId: null,
    groupName: null,
    groupOrder: null,
    userId: null,
    userName: null,
    delFlag: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null,
  }
  proxy.resetForm('groupRef')
}
/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}
/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}
// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.groupId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}
/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = '添加设备分组'
}
/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const groupId = row.groupId || ids.value
  getGroup(groupId).then((response) => {
    form.value = response.data
    open.value = true
    title.value = '修改设备分组'
  })
}
/** 选择设备 */
function selectDevice(row) {
  // 刷新子组件
  proxy.$refs['groupDeviceList'].show(row);
}
/** 提交按钮 */
function submitForm() {
  proxy.$refs['groupRef'].validate((valid) => {
    if (valid) {
      if (form.value.groupId != null) {
        updateGroup(form.value).then((response) => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addGroup(form.value).then((response) => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}
/** 删除按钮操作 */
function handleDelete(row) {
  const groupIds = row.groupId || ids.value
  proxy.$modal.confirm('是否确认删除设备分组编号为"' + groupIds + '"的数据项？')
    .then(function () {
      return delGroup(groupIds)
    })
    .then(() => {
      getList()
      proxy.$modal.msgSuccess('删除成功')
    })
    .catch(() => { })
}
/** 导出按钮操作 */
function handleExport() {
  proxy.download('iot/group/export', {
      ...queryParams.value,
    }, `group_${new Date().getTime()}.xlsx`
  )
}

getList();
init();
</script>
