export function loadBMap() {
    return new Promise(function(resolve, reject) {
        // if (typeof BMap !== 'undefined') {
        //     resolve(BMap)
        //     return true
        // }
        // window.onBMapCallback = function() {
        //     resolve(BMap)
        // }
        // // 使用https协议需要添加一下meta标签
        // var protocolStr = document.location.protocol;
        // if(protocolStr == "https:")
        // {
        //     let meta = document.createElement('meta')
        //     meta.httpEquiv = 'Content-Security-Policy'
        //     meta.content ='upgrade-insecure-requests'
        //     meta.onerror = reject
        //     document.head.appendChild(meta)
        // }
        // 引入高德地图
        let script = document.createElement('script')
        script.type = 'text/javascript'
        script.src = 'https://webapi.amap.com/maps?v=2.0&key=2cd74056ead5ced85589e398b621c955'
        script.onerror = reject
        document.head.appendChild(script)
    })
}
