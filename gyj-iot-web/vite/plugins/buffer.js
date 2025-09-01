import { nodePolyfills } from 'vite-plugin-node-polyfills';

export default function createBuffer() {
    return nodePolyfills({  // 新增插件配置
        include: ['buffer'], // 只 polyfill Buffer
        globals: { Buffer: true }, // 确保全局 Buffer 可用
    })
}
