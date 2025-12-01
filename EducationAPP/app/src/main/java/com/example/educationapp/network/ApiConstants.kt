package com.example.educationapp.network

/**
 * API常量配置
 */
object ApiConstants {
    // 智谱AI BigModel API配置
    const val DEEPSEEK_BASE_URL = "https://open.bigmodel.cn/api/paas/v4/"
    
    // 你的智谱AI API密钥
    const val DEEPSEEK_API_KEY = "64ebb924bbe64f82823048acea7fd27f.pcZZhvMz2OMYIuOl"
    
    // API端点 - 智谱AI标准格式
    const val CHAT_ENDPOINT = "chat/completions"
    
    // 模型配置 - 智谱AI免费模型选项
    const val DEFAULT_MODEL = "glm-4-flash"  // 最新免费快速模型
    const val ALT_MODEL_1 = "glm-4-plus"     // 备用模型1
    const val ALT_MODEL_2 = "chatglm_turbo"  // 备用模型2
    const val MAX_TOKENS = 1000
    const val TEMPERATURE = 0.7f
    
    // 请求头
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val CONTENT_TYPE_JSON = "application/json"
}
