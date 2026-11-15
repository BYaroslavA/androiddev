# Дослідження та інтеграція Топ-5 сучасних сторонніх бібліотек для Android

Навчальний проєкт, розроблений у межах виконання домашнього завдання з дослідження сучасних відкритих бібліотек на GitHub для Android-розробки.

У проєкті реалізовано повноцінний інтерактивний дашборд, який наочно демонструє роботу 5 найпопулярніших сторонніх бібліотек різного профілю: векторна анімація, візуалізація даних, оптимізація завантаження медіа, UX-скелетони та фізика часток.

---

## 🏆 Топ-5 бібліотек: Огляд та обґрунтування вибору

Було обрано 5 високоефективних відкритих бібліотек з активною підтримкою спільноти, високим рейтингом на GitHub та широким застосуванням у комерційній розробці:

| № | Бібліотека | GitHub | Призначення | Переваги |
|---|---|---|---|---|
| **1** | **Lottie Android** | [airbnb/lottie-android](https://github.com/airbnb/lottie-android) | Векторна анімація After Effects | Рендеринг складної графіки з Bodymovin JSON у реальному часі без просідання FPS |
| **2** | **MPAndroidChart** | [PhilJay/MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) | Візуалізація даних та графіки | Потужний рушій побудови інтерактивних графіків із підтримкою жестів та анімацій |
| **3** | **Coil** | [coil-kt/coil](https://github.com/coil-kt/coil) | Асинхронне завантаження зображень | Kotlin-first підхід, побудований на Coroutines, надлегкий та швидкий |
| **4** | **Facebook Shimmer** | [facebook/shimmer-android](https://github.com/facebook/shimmer-android) | Skeleton Loading ефекти | Створення ефекту мерехтіння для плейсхолдерів під час завантаження даних |
| **5** | **Konfetti** | [DanielMartinus/Konfetti](https://github.com/DanielMartinus/Konfetti) | Фізика часток та святкові ефекти | Легковаговий генератор часток (конфеті) для гейміфікації та винагороди дій |

---

## 🛠️ Як бібліотеки підключено до проєкту

### 1. Додавання репозиторіїв (`settings.gradle.kts`)
Для бібліотеки `MPAndroidChart` потрібен репозиторій **JitPack**:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Оголошення залежностей у Gradle Version Catalog (`gradle/libs.versions.toml`)

```toml
[versions]
lottie = "6.6.2"
mpAndroidChart = "v3.1.0"
coil = "2.7.0"
shimmer = "0.5.0"
konfetti = "2.0.4"

[libraries]
lottie = { group = "com.airbnb.android", name = "lottie", version.ref = "lottie" }
mpandroidchart = { group = "com.github.PhilJay", name = "MPAndroidChart", version.ref = "mpAndroidChart" }
coil = { group = "io.coil-kt", name = "coil", version.ref = "coil" }
shimmer = { group = "com.facebook.shimmer", name = "shimmer", version.ref = "shimmer" }
konfetti-xml = { group = "nl.dionsegijn", name = "konfetti-xml", version.ref = "konfetti" }
```

### 3. Підключення в модуль (`app/build.gradle.kts`)

```kotlin
dependencies {
    implementation(libs.lottie)
    implementation(libs.mpandroidchart)
    implementation(libs.coil)
    implementation(libs.shimmer)
    implementation(libs.konfetti.xml)
}
```

### 4. Дозвіл на доступ до Інтернету (`AndroidManifest.xml`)

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 💻 Детальний опис використання в додатку

Головний екран реалізовано в `MainActivity.kt` та `activity_main.xml`. Кожна бібліотека представлена окремою інтерактивною карткою:

### 1. Lottie Android (`com.airbnb.android:lottie`)
* **Що робить у додатку:** Відтворює векторну анімацію пульсуючої та обертової зірки безпосередньо з JSON-файлу (`res/raw/lottie_star.json`).
* **Інтерактивні елементи:**
  * Кнопка **«Пауза / Відтворити»** контролює стан відтворення (`lottieView.pauseAnimation()` / `lottieView.resumeAnimation()`).
  * Кнопка **«Швидкість»** циклічно перемикає темп: `0.5x` → `1.0x` → `1.5x` → `2.0x`.

### 2. MPAndroidChart (`com.github.PhilJay:MPAndroidChart`)
* **Що робить у додатку:** Будує гістограму (`BarChart`) тижневої активності користувачів (Пн-Нд).
* **Стилізація:** Налаштовано палітру кольорів Material 3, вимкнено зайві рамки й сітки, підключено текстовий форматер осей (`IndexAxisValueFormatter`).
* **Інтерактивні елементи:**
  * Кнопка **«Анімувати»** запускає плавне зростання стовпчиків (`barChart.animateY(1200)`).
  * Кнопка **«Оновити дані»** генерує новий випадковий розподіл значень з перемальовкою графіка.

### 3. Coil (`io.coil-kt:coil`)
* **Що робить у додатку:** Асинхронно завантажує фото з віддаленого джерела з підтримкою кешування, плавної появи (`crossfade(true)`) та заглушок.
* **Інтерактивні елементи:**
  * Кнопка **«Форма (Коло/Радіус)»** динамічно змінює трансформацію зображення між круглою (`CircleCropTransformation()`) та заокругленим прямокутником (`RoundedCornersTransformation(32f)`).
  * Кнопка **«Змінити фото»** підвантажує нове випадкове зображення.

### 4. Facebook Shimmer (`com.facebook.shimmer:shimmer`)
* **Що робить у додатку:** Демонструє патерн Skeleton Loading. Замість порожнього екрана або спінера користувач бачить мерехтливі силуети майбутнього контенту.
* **Інтерактивні елементи:**
  * Кнопка **«Перемкнути: Завантаження / Готово»** вмикає/зупиняє мерехтіння (`startShimmer()` / `stopShimmer()`) і перемикає видимість між плейсхолдерами та завантаженою карткою користувача.

### 5. Konfetti (`nl.dionsegijn:konfetti-xml`)
* **Що робить у додатку:** Забезпечує фізичну симуляцію частинок поверх усього інтерфейсу (`KonfettiView`).
* **Інтерактивні елементи:**
  * Кнопка **«Вибух 🎉»** запускає розліт 100 різнокольорових частинок у радіусі 360 градусів.
  * Кнопка **«Злива 🎊»** запускає безперервний потік часток, які плавно падають з верхнього краю екрана.

---

## 📸 Скріншоти роботи додатку

> Для додавання скріншотів покладіть зображення в папку `screenshots/` у корені репозиторію.

| Екран (Загальний вигляд) | Графіки та Lottie | Скелетон Shimmer / Coil | Святкове конфеті |
|:---:|:---:|:---:|:---:|
| ![Загальний вигляд](<img width="974" height="994" alt="image" src="https://github.com/user-attachments/assets/19718691-bb89-43b9-ba3a-98e0becd5065" />
) | ![Графіки](<img width="381" height="330" alt="image" src="https://github.com/user-attachments/assets/b6b5218c-820f-40f6-bd68-1c02c1279344" />
) | ![Shimmer і Coil](<img width="380" height="214" alt="image" src="https://github.com/user-attachments/assets/1d61b02c-327c-491d-8c09-1a0ff7b002a1" />
) | ![Конфеті](<img width="487" height="932" alt="image" src="https://github.com/user-attachments/assets/56e6b787-9467-4b58-9bfa-744bc9ab5840" />
) |

---

## ⚙️ Стек технологій
* **Мова програмування:** Kotlin 2.x
* **UI Toolkit:** Android Views, Material Design 3 (Material Components)
* **Build System:** Gradle (Kotlin DSL `.kts`) + Version Catalog (`libs.versions.toml`)
* **Мінімальна версія SDK:** API 33 (Android 13)
* **Цільова версія SDK:** API 37

---

## 🚀 Інструкція із запуску проєкту
1. Клонувати репозиторій:
   ```bash
   git clone https://github.com/BYaroslavA/androiddev.git
   ```
2. Відкрити проєкт в **Android Studio** (Ladybug / Meerkat або новішої версії).
3. Дочекатися завершення синхронізації Gradle.
4. Запустити на емуляторі або фізичному пристрої з Android 13+ (комбінація `Shift + F10` або кнопка **Run** ▶️).
