# 知识调研报告：Android维修记录应用

## 一、技术栈识别
- 主要技术：Kotlin 1.9.20 + Jetpack Compose + Material 3
- 相关依赖：Room 2.6.1, Navigation Compose 2.7.6, Coil 2.5.0, Accompanist Permissions 0.32.0
- 技术领域：Android移动应用开发、本地数据持久化、语音识别、图像处理

## 二、权威知识源
| 来源 | URL | 用途 |
|------|-----|------|
| Android官方文档 | https://developer.android.com | API参考、最佳实践 |
| Kotlin官方文档 | https://kotlinlang.org/docs/ | 语言特性 |
| Room官方指南 | https://developer.android.com/training/data-storage/room | 数据库操作 |

## 三、核心知识点
### 3.1 概念理解
- **Jetpack Compose**：Android现代声明式UI框架，使用Kotlin DSL构建UI
- **Material 3**：最新Material Design组件库，支持动态颜色和圆角设计
- **Room**：SQLite抽象层，提供编译时SQL验证和Flow响应式查询
- **MVVM架构**：ViewModel管理UI状态，Repository封装数据访问

### 3.2 API/用法
```kotlin
@Composable
fun Screen(viewModel: ViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    // 声明式UI渲染
}

@Entity
data class Record(@PrimaryKey val id: Long, val title: String)

@Dao
interface RecordDao {
    @Query("SELECT * FROM record")
    fun getAll(): Flow<List<Record>>
}
```

### 3.3 最佳实践
- ✅ 使用StateFlow管理UI状态，支持响应式更新
- ✅ 使用ViewModelProvider.Factory手动注入依赖
- ✅ 使用FileProvider安全共享相机拍摄的照片
- ✅ 使用ActivityResultContracts处理权限和结果

### 3.4 常见陷阱
- ❌ 忘记添加ksp插件导致Room编译失败
- ❌ 在Composable中直接创建ViewModel导致状态丢失
- ❌ 忘记申请RECORD_AUDIO权限导致语音识别失败

## 四、推荐实现方案
1. **数据层**：Room数据库存储维修记录，支持标题、内容、设备名、图片路径
2. **业务层**：Repository封装DAO操作，ViewModel管理UI状态和业务逻辑
3. **UI层**：Jetpack Compose + Material 3构建现代化界面
4. **语音输入**：使用RecognizerIntent调用系统语音识别
5. **图片处理**：CameraX拍照 + Coil图片加载 + FileProvider文件共享

## 五、版本兼容性
- minSdk: 26 (Android 8.0)
- targetSdk: 34 (Android 14)
- Kotlin: 1.9.20
- Compose Compiler: 1.5.5
