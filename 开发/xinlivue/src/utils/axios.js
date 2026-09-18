import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service = axios.create({
    baseURL: '/api',
    timeout: 5000
})

// 请求拦截：自动带上token
service.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if(token) {
        config.headers.token = token
    }
    return config
})

// 响应拦截：捕获未登录 + 区分各类网络/服务异常
service.interceptors.response.use(res => {
    // 业务层面返回未登录
    if(res.data.code === 400 && res.data.msg === '请先登录') {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        ElMessage.warning("登录已失效，请重新登录")
        router.push('/login')
    }
    return res
}, err => {
    if (!err.response) {
        // 没有response：连不上服务：后端没启动、端口不对、跨域、断网、超时
        if(err.message.includes('timeout')){
            ElMessage.error("请求超时，请检查后端服务")
        }else{
            ElMessage.error("无法连接后端，请确认SpringBoot已经启动在8080端口")
        }
    } else {
        // 有response，后端收到请求返回http错误码
        const status = err.response.status
        // 如果后端有返回错误消息，优先展示后端给的msg
        if(err.response.data && err.response.data.msg){
            ElMessage.error(err.response.data.msg)
        }else{
            switch (status){
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

