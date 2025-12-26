import request from '@/utils/request'

// 获取平台名称
export function getPlatformName() {
  return request({
    url: '/platformName',
    headers: {
      isToken: false
    },
    method: 'get',
    timeout: 20000
  })
}
