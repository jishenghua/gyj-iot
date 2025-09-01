import request from '@/utils/request'

// 注册方法
export function register(data) {
  return request({
    url: '/iot/tool/register',
    headers: {
      isToken: false
    },
    method: 'post',
    data: data
  })
}

// 查询用户列表
export function listUser(query) {
  return request({
    url: '/iot/tool/userList',
    method: 'get',
    params: query
  })
}

// 获取所有下发的topic
export function getTopics(params){
  return request({
    url: '/iot/tool/getTopics',
    method: 'get',
    params: params,
  })
}

// 获取所有下发的topic
export function decode(params){
  return request({
    url: '/iot/tool/decode',
    method: 'get',
    params: params,
  })
}



