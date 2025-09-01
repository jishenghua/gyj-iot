<template>
<el-dialog title="选择用户" :visible.sync="openSelectUser" width="800px">
    <div style="margin-top:-50px;">
        <el-divider></el-divider>
    </div>
    <!--用户数据-->
    <el-form :model="queryParams" ref="queryForm" :rules="rules" :inline="true" label-width="80px">
        <el-form-item label="手机号码" prop="phonenumber">
            <el-input type="text" placeholder="请输入用户手机号码" v-model="queryParams.phonenumber" minlength="10" clearable size="small" show-word-limit style="width: 240px" @keyup.enter.native="handleQuery"></el-input>
        </el-form-item>
        <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="userList" highlight-current-row size="mini" @current-change="handleCurrentChange" border>
        <el-table-column label="选择" width="50" align="center">
            <template slot-scope="scope">
                <input type="radio" :checked="scope.row.isSelect" name="user" />
            </template>
        </el-table-column>
        <el-table-column label="用户编号" align="center" key="userId" prop="userId" width="120" />
        <el-table-column label="用户名称" align="center" key="userName" prop="userName"  />
        <el-table-column label="用户昵称" align="center" key="nickName" prop="nickName" />
        <el-table-column label="手机号码" align="center" key="phonenumber" prop="phonenumber" width="120" />
        <el-table-column label="创建时间" align="center" prop="createTime" width="160">
            <template slot-scope="scope">
                <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
        </el-table-column>

    </el-table>
    <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleAddDeviceUser">分享</el-button>
        <el-button @click="closeSelectUser">关 闭</el-button>
    </div>
</el-dialog>
</template>

<script setup name="UserList">
import { listUser } from "@/api/iot/tool";
import { addDeviceUser, addDeviceUsers } from "@/api/iot/deviceuser";

const { proxy } = getCurrentInstance();

// 遮罩层
const loading = ref(false);
// 选中数组
const ids = ref([]);
// 弹出层标题
const title = ref("");
// 用户列表
const userList = ref([]);
// 选中的用户
const user = ref({});
// 设备信息
const deviceInfo = ref({});
// 是否显示选择用户弹出层
const openSelectUser = ref(false);
// 总条数
const total = ref(0);

const data = reactive({
    // 查询参数
    queryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: undefined,
        phonenumber: undefined,
        status: 0,
        deptId: undefined
    },
    // 表单校验
    rules: {
        phonenumber: [{
            required: true,
            message: "手机号码不能为空",
            trigger: "blur"
        }, {
            min: 11,
            max: 11,
            message: '手机号码长度为11位',
            trigger: 'blur'
        }],
    }
})

const { queryParams, rules } = toRefs(data);

const props = defineProps({
    device: {
        type: Array,
        default: null
    }
});

// 获取到父组件传递的device
watch(() => props.device, (newVal, oldVal) => {
    deviceInfo.value = newVal;
});

/** 查询用户列表 */
function getList() {
    loading.value = true;
    listUser(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
        userList.value = response.rows;
        total.value = response.total;
        loading.value = false;
    });
}
/** 搜索按钮操作 */
function handleQuery() {
    proxy.$refs["queryForm"].validate(valid => {
        if (valid) {
            queryParams.value.pageNum = 1;
            getList();
        }
    });
}
// 重置查询
function resetQuery() {
    proxy.$refs["queryForm"].resetFields();
    userList.value = [];
}
//设置单选按钮选中
function setRadioSelected(userId) {
    for (let i = 0; i < userList.value.length; i++) {
        if (userList.value[i].userId == userId) {
            userList.value[i].isSelect = true;
            user.value = userList.value[i];
        } else {
            userList.value[i].isSelect = false;
        }
    }
}
// 单选数据
function handleCurrentChange(user) {
    if (user != null) {
        setRadioSelected(user.userId);
        user.value = user;
    }
}
// 关闭选择用户
function closeSelectUser() {
    openSelectUser.value = false;
    resetQuery();
}
// 添加设备用户
function handleAddDeviceUser() {
    if (deviceInfo.value != null && deviceInfo.value.length > 0 && user.value != null) {
        if (deviceInfo.value.length == 1) {
            var form = {};
            form.deviceId = deviceInfo.value[0].deviceId;
            form.deviceName = deviceInfo.value[0].deviceName;
            form.userId = user.value.userId;
            form.userName = user.value.userName;
            form.phonenumber=user.value.phonenumber;
            addDeviceUser(form).then(response => {
                proxy.$modal.msgSuccess("新增成功");
                resetQuery();
                openSelectUser.value = false;
                proxy.$parent.getList();
            });
        } else {
            var form = [];
            deviceInfo.value.forEach(device => {
                let data = {};
                data.deviceId = device.deviceId;
                data.deviceName = device.deviceName
                data.userId = user.value.userId;
                data.userName = user.value.userName;
                data.phonenumber = user.value.phonenumber;
                form.push(data);
            });

            addDeviceUsers(form).then(response => {
                proxy.$modal.msgSuccess("新增成功");
                resetQuery();
                openSelectUser.value = false;
                proxy.$parent.getList();
            });

        }
    } else {
        openSelectUser.value = false;
    }
}
</script>
