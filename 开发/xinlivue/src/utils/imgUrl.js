// 图片 URL 工具：统一处理带 / 不带 / 的情况
// 返回相对路径，走 Vite 代理，浏览器自动用当前域名
export function imgUrl(p) {
    if (!p) return ''
    if (p.startsWith('http')) return p          // 完整 URL，原样返回
    if (p.startsWith('/')) return p              // 已有前导 /，直接用
    return '/' + p                                // 没 / 就补一个
}