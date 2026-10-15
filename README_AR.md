# J.A.R.V.I.S MARK LX — Android Project

دي ملفات مشروع أندرويد قابلة للبناء وليست APK جاهزًا. اسم MARK LX هو رقم روماني 60، أعلى من LV (55).

## الذكاء الاصطناعي
- يستخدم Gemini API بالموديل `gemini-3.8-flash` حسب قائمة موديلات Google الرسمية. قد يختلف توفر الموديل/حدود الاستخدام حسب حسابك والمنطقة.
- أول تشغيل يطلب API Key، ويحفظه مشفرًا محليًا على الجهاز باستخدام AndroidX Security/Android Keystore. لا ترفع المفتاح إلى GitHub ولا ترسله في المحادثة.
- يدعم إدخال الصوت عبر خدمة التعرف على الكلام في أندرويد، وردًا صوتيًا عبر TextToSpeech.

## تنزيل Android Studio
نزّل النسخة الرسمية من: https://developer.android.com/studio
على Windows اختر ملف `.exe`. صفحة Google تذكر حدًا أدنى قدره 8 GB مساحة فارغة لـ Android Studio وحده، و16 GB إذا أضفت المحاكي؛ وذاكرة RAM لا تقل عن 8 GB. تنزيل SDK سيضيف وقتًا ومساحة إضافية.

## خطوات البناء على Windows
1. ثبّت Android Studio وافتحه.
2. فك ضغط ملف المشروع بالكامل.
3. من Android Studio اختر Open وحدد مجلد `JARVIS_MARK_LX` الذي يحتوي على `settings.gradle`.
4. وافق على تنزيل Android SDK المطلوبة وانتظر مزامنة Gradle.
5. اختر Build → Build Bundle(s) / APK(s) → Build APK(s).
6. عند نجاح البناء، افتح `app/build/outputs/apk/debug/app-debug.apk` وثبّته على جهاز Android.

## بديل بناء من المتصفح عبر GitHub Actions
يوجد ملف workflow في `.github/workflows/build-apk.yml`. ارفع المشروع إلى مستودع GitHub، ثم افتح تبويب Actions وشغّل Build JARVIS MARK LX APK. بعد النجاح نزّل الـ artifact باسم `JARVIS-MARK-LX-debug-apk`. لا ترفع API Key إلى المستودع.

## ملاحظات مهمة
- لم يتم إنتاج أو اختبار APK نهائي داخل بيئة إنشاء الملفات هذه؛ هذا أرشيف مصدر.
- لا يوجد اختبار اتصال بمفتاح API حقيقي.
- تخزين المفتاح مشفر محليًا أفضل من نص صريح، لكنه لا يضمن استحالة استخراجه من جهاز مخترق.
- الميزات التي تحتاج صلاحيات نظام شاملة أو التحكم في تطبيقات أخرى غير متاحة تلقائيًا.
