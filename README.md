# LinkPlay
تطبيق Android عربي لاستقبال روابط فيديو GitHub عبر اللصق أو المشاركة وتشغيلها داخل التطبيق أو عبر مشغّل خارجي.

## بناء APK
يفتح GitHub Actions سير العمل Build LinkPlay APK عند الدفع إلى main أو يدوياً. بعد نجاحه نزّل Artifact باسم LinkPlay-debug-apk.

## ملاحظات
هذا إصدار debug أولي. التشغيل يعتمد على صلاحية الرابط ودعم الخادم للبث وHTTP Range؛ بعض روابط GitHub Releases قد تُحوّل إلى CDN أو لا تدعم التشغيل المباشر. النسخة تقبل فيديوهات MP4 وM4V وWebM وMOV.
