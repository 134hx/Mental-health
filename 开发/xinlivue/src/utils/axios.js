import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service = axios.create({
    baseURL: '/api',
    timeout: 15000
})

// 请求拦截：自动带上token
service.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.token = token
    }
    return config
})

// 响应拦截
service.interceptors.response.use(res => {
    const result = res.data

    // 未登录，跳转登录页
    if (result.code === 400 && result.msg === '请先登录') {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        ElMessage.warning("登录已失效，请重新登录")
        router.push('/login')
        // 登录这里继续new Error没问题，不会在业务组件拿response
        return Promise.reject(new Error(result.msg))
    }

    // 业务码不等于200：直接把 result {code,msg} reject抛出去，不要包new Error！
    if (result.code !== 200) {
        return Promise.reject(result)
    }

    return res

}, err => {
    // http层面错误（404 500 超时 断网）保留原样
    if (!err.response) {
        if (err.message.includes('timeout')) {
            ElMessage.error("请求超时，请检查后端服务")
        } else {
            ElMessage.error("无法连接后端，请确认SpringBoot已经启动在8080端口")
        }
    } else {
        const status = err.response.status
        if (err.response.data && err.response.data.msg) {
            ElMessage.error(err.response.data.msg)
        } else {
            switch (status) {
                case 404:
                    ElMessage.error("接口地址不存在 404")
                    break
                case 500:
                    ElMessage.error("服务器内部错误500，请查看后端控制台报错")
                    break
                case 403:
                    ElMessage.error("权限不足403")
                    break
                default:
                    ElMessage.error(`请求错误：${status}`)
            }
        }
    }
    return Promise.reject(err)
})

export default service
