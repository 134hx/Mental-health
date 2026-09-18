import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/login',
        component: () => import("@/components/login/Login.vue")
    },
    {
        path: '/psych',
        redirect: '/psych/Index',
        children: [
            {
                path: 'index',
                component: () => import("@/components/psych/Index.vue")
            },
            {
                path: '/resetPwd',
                component: () => import("@/components/login/ResetPwd.vue")
            },
            {
                path: 'psyindex',
                component: () => import("@/components/psychology/PsychologyIndex.vue")
            },
            {
                path: 'test',
                component: () => import("@/components/psychology/PsychologyTest.vue")
            },
            {
                path: 'record',
                component: () => import("@/components/psychology/PsychRecord.vue")
            },
            {
                path: 'report',
                component: () => import("@/components/psychology/PsychReport.vue")
            },
            {
                path: 'aichat',
                component: () => import("@/components/aichat/AiChat.vue")
            },
            {
                path: 'userinfo',
                component: () => import("@/components/user/UserInfo.vue")
            }
        ]
    },
    {
        path: '/',
        redirect: '/login'
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router


