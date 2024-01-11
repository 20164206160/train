import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css';
import * as Icons from '@ant-design/icons-vue';


// createApp(App).use(Antd).use(store).use(router).mount('#app')

const app = createApp(App);
app.use(Antd).use(store).use(router).mount('#app');

// 全局使用图标
const icons = Icons;
for (const i in icons) {
    app.component(i, icons[i]);
}