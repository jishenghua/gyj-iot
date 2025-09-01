<template>
    <div style="border: 0px solid #ebebeb; overflow: hidden; border-radius: 6px; background-color: #ebebeb; padding: 8px 5px 8px 0;">
        <v-ace-editor ref="codeEditor" v-model:value="currentContent" :options="options" :lang="lang" :codeStyle="'chrome'" style="width: 100%; height: 450px" />
    </div>
</template>

<script setup name="AceEditor">
import { VAceEditor } from 'vue3-ace-editor';
import 'ace-builds/src-noconflict/ext-language_tools'
import 'ace-builds/src-noconflict/mode-groovy'
import 'ace-builds/src-noconflict/mode-mysql'
import 'ace-builds/src-noconflict/mode-json'
import 'ace-builds/src-noconflict/theme-chrome'
import 'ace-builds/src-noconflict/snippets/groovy'
import 'ace-builds/src-noconflict/snippets/json'
import 'ace-builds/src-noconflict/ext-beautify'

const { proxy } = getCurrentInstance();

const currentContent = ref(`import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.core.util.NumberUtil;

// 1. 获取主题和内容(必要)
String topic = msgContext.getTopic();
String payload = msgContext.getPayload();


// 2. 数据转换(自己处理)
msgContext.logger.debug("数据转换处理")
String NewTopic = topic;
String NewPayload = payload;


// 3. 返回新的数据（必要）
msgContext.setTopic(NewTopic);
msgContext.setPayload(NewPayload);`);

const props = defineProps({
    width: {
        type: String,
        default: '100%',
    },
    height: {
        type: String,
        default: '500px',
    },
    content: {
        type: String,
        required: true,
        default: () => null,
    },
    lang: {
        type: String,
        default: 'groovy',
    },
    readOnly: {
        type: Boolean,
        default: false,
    },
    codeStyle: {
        type: String,
        default: 'chrome',
    },
});

const options = ref({
    autoScrollEditorIntoView: true,
    // enableBasicAutocompletion: true,
    enableLiveAutocompletion: true,
    enableSnippets: true,
    // 只读
    readOnly: props.readOnly,
    // 显示打印边距线
    showPrintMargin: false,
    // 字体大小
    fontSize: 13,
})
</script>
