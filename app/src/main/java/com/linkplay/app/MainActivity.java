package com.linkplay.app;

import android.content.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import org.json.*;

public class MainActivity extends AppCompatActivity {
    TextInputEditText input, titleInput;
    TextView status, browserLink;
    LinearLayout savedList;
    final int bg=0xFF07111F, panel=0xFF0F1F31, green=0xFF10B981, white=0xFFF1F5F9, muted=0xFF94A3B8;
    final String PREFS="linkplay_saved", KEY="items";
    @Override public void onCreate(Bundle b) {
        super.onCreate(b); getWindow().setStatusBarColor(bg); getWindow().setNavigationBarColor(bg);
        ui(); renderSaved(); handle(getIntent());
    }
    @Override protected void onNewIntent(Intent i) { super.onNewIntent(i); setIntent(i); handle(i); }
    GradientDrawable shape(int c,int r) { GradientDrawable d=new GradientDrawable(); d.setColor(c); d.setCornerRadius(dp(r)); d.setStroke(dp(1),0xFF233649); return d; }
    int dp(float n) { return (int)(n*getResources().getDisplayMetrics().density+.5f); }
    TextView text(String value,int size,int color,boolean bold) {
        TextView t=new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if(bold)t.setTypeface(null,1); t.setGravity(Gravity.RIGHT); return t;
    }
    void ui() {
        ScrollView scroll=new ScrollView(this); LinearLayout root=new LinearLayout(this); root.setOrientation(1);
        root.setPadding(dp(20),dp(24),dp(20),dp(28)); root.setBackgroundColor(bg); scroll.addView(root); setContentView(scroll);
        TextView brand=text("LINKPLAY",14,green,true); brand.setLetterSpacing(.18f); brand.setGravity(Gravity.LEFT); root.addView(brand);
        TextView title=text("شغّل الرابط، لا تحمّله",27,white,true); LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2);tp.topMargin=dp(22);root.addView(title,tp);
        TextView sub=text("الصق رابط الفيديو أو شاركه إلى التطبيق. أنشئ رابط تشغيل للمتصفح واحفظ روابطك بعناوين.",14,muted,false);sub.setLineSpacing(dp(4),1);
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.topMargin=dp(8);root.addView(sub,sp);
        LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setPadding(dp(15),dp(16),dp(15),dp(16));card.setBackground(shape(panel,18));
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.topMargin=dp(22);root.addView(card,cp);
        card.addView(text("رابط الفيديو",14,white,true));
        input=new TextInputEditText(this);input.setTextColor(white);input.setHintTextColor(muted);input.setHint("https://github.com/.../video.mp4");
        input.setTextSize(13);input.setMinLines(2);input.setGravity(Gravity.RIGHT|Gravity.TOP);input.setPadding(dp(12),dp(12),dp(12),dp(12));input.setBackground(shape(0xFF081524,12));
        LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(-1,-2);ip.topMargin=dp(10);card.addView(input,ip);
        MaterialButton paste=button("لصق الرابط",0xFF233649,white);addButton(card,paste,10);paste.setOnClickListener(v->paste());
        card.addView(text("عنوان الحفظ (اختياري)",14,white,true));
        titleInput=new TextInputEditText(this);titleInput.setTextColor(white);titleInput.setHintTextColor(muted);titleInput.setHint("مثال: محاضرة الفصل الخامس");
        titleInput.setSingleLine(true);titleInput.setTextSize(14);titleInput.setGravity(Gravity.RIGHT);titleInput.setPadding(dp(12),dp(12),dp(12),dp(12));titleInput.setBackground(shape(0xFF081524,12));
        LinearLayout.LayoutParams tip=new LinearLayout.LayoutParams(-1,-2);tip.topMargin=dp(9);card.addView(titleInput,tip);
        MaterialButton save=button("حفظ الرابط بالعنوان",green,0xFF031914);addButton(card,save,12);save.setOnClickListener(v->saveCurrent());
        MaterialButton play=button("تشغيل داخل LinkPlay",0xFF176B54,white);addButton(root,play,14);play.setOnClickListener(v->internal());
        MaterialButton ext=button("فتح بمشغّل خارجي",0xFF233649,white);addButton(root,ext,8);ext.setOnClickListener(v->external());
        MaterialButton web=button("إنشاء رابط تشغيل للمتصفح",green,0xFF031914);addButton(root,web,8);web.setOnClickListener(v->makeBrowserLink());
        browserLink=text("",12,green,false);browserLink.setTextIsSelectable(true);LinearLayout.LayoutParams blp=new LinearLayout.LayoutParams(-1,-2);blp.topMargin=dp(8);root.addView(browserLink,blp);
        MaterialButton copy=button("نسخ رابط المتصفح",0xFF233649,white);addButton(root,copy,5);copy.setOnClickListener(v->{String s=browserLink.getText().toString();if(s.startsWith("https://")){((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("LinkPlay browser link",s));status.setText("تم نسخ رابط المتصفح.");}else status.setText("أنشئ رابط المتصفح أولاً.");});
        status=text("يدعم ملفات الفيديو العامة التي يسمح خادمها بالبث.",13,muted,false);status.setLineSpacing(dp(3),1);LinearLayout.LayoutParams st=new LinearLayout.LayoutParams(-1,-2);st.topMargin=dp(12);root.addView(status,st);
        TextView savedHeading=text("الروابط المحفوظة",19,white,true);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.topMargin=dp(28);root.addView(savedHeading,hp);
        savedList=new LinearLayout(this);savedList.setOrientation(1);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(12);root.addView(savedList,lp);
        TextView note=text("مهم: رابط المتصفح يفتح صفحة مشغّل ويب، لكن تشغيل الفيديو يعتمد على أن الرابط مباشر وأن الخادم يدعم تشغيل الفيديو عبر المتصفح.",12,muted,false);note.setLineSpacing(dp(3),1);LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2);np.topMargin=dp(18);root.addView(note,np);
    }
    void addButton(LinearLayout parent,MaterialButton b,int top){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(48));p.topMargin=dp(top);parent.addView(b,p);}
    MaterialButton button(String t,int color,int fg){MaterialButton b=new MaterialButton(this);b.setText(t);b.setTextColor(fg);b.setTextSize(14);b.setCornerRadius(dp(12));b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));return b;}
    void handle(Intent i){if(i==null)return;String v=null;if(Intent.ACTION_SEND.equals(i.getAction()))v=i.getStringExtra(Intent.EXTRA_TEXT);else if(Intent.ACTION_VIEW.equals(i.getAction())&&i.getData()!=null)v=i.getData().toString();String u=extract(v);if(u!=null&&input!=null){input.setText(u);status.setText("تم استلام الرابط. اختر طريقة التشغيل أو احفظه بعنوان.");}}
    String extract(String t){if(t==null)return null;java.util.regex.Matcher m=java.util.regex.Pattern.compile("https?://[^\\s<>\\\"']+").matcher(t.trim());return m.find()?m.group().replaceAll("[),.]+$",""):null;}
    String valid(){String raw=input.getText()==null?"":input.getText().toString().trim(),u=extract(raw);if(u==null){status.setText("ألصق رابط HTTPS صحيحاً أولاً.");return null;}Uri x=Uri.parse(u);String h=x.getHost();if(!"https".equalsIgnoreCase(x.getScheme())||h==null||!(h.equalsIgnoreCase("github.com")||h.equalsIgnoreCase("raw.githubusercontent.com")||h.equalsIgnoreCase("objects.githubusercontent.com")||h.endsWith(".githubusercontent.com"))){status.setText("أدخل رابط HTTPS مباشر من GitHub أو GitHub Releases.");return null;}String path=x.getPath()==null?"":x.getPath().toLowerCase();if(!path.matches(".*\\.(mp4|m4v|webm|mov|m3u8)(/.*)?$")){status.setText("يجب أن يكون الرابط لملف فيديو مباشر مثل MP4 أو WebM، وليس صفحة Release.");return null;}return u;}
    void internal(){String u=valid();if(u==null)return;hideKeyboard();Intent i=new Intent(this,PlayerActivity.class);i.putExtra("url",u);startActivity(i);}
    void external(){String u=valid();if(u==null)return;Intent i=new Intent(Intent.ACTION_VIEW);i.setDataAndType(Uri.parse(u),"video/*");try{startActivity(Intent.createChooser(i,"اختيار مشغّل الفيديو"));}catch(Exception e){status.setText("ماكو مشغّل خارجي مناسب مثبت.");}}
    void makeBrowserLink(){String u=valid();if(u==null)return;String link="https://3c5-o.github.io/LinkPlay/player/?url="+Uri.encode(u);browserLink.setText(link);status.setText("تم إنشاء رابط صفحة المشغّل. قد يحتاج نشر صفحة الويب أولاً حتى يصبح متاحاً.");}
    void saveCurrent(){String u=valid();if(u==null)return;String name=titleInput.getText()==null?"":titleInput.getText().toString().trim();if(name.isEmpty()){String p=Uri.parse(u).getLastPathSegment();name=(p==null||p.isEmpty())?"فيديو محفوظ":p;}JSONArray arr=readSaved();for(int i=arr.length()-1;i>=0;i--){JSONObject o=arr.optJSONObject(i);if(o!=null&&u.equals(o.optString("url")))arr.remove(i);}JSONObject item=new JSONObject();try{item.put("title",name);item.put("url",u);item.put("savedAt",System.currentTimeMillis());arr.put(item);}catch(JSONException ignored){}getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(KEY,arr.toString()).apply();renderSaved();status.setText("تم حفظ الرابط بعنوان: "+name);}
    JSONArray readSaved(){try{return new JSONArray(getSharedPreferences(PREFS,MODE_PRIVATE).getString(KEY,"[]"));}catch(Exception e){return new JSONArray();}}
    void renderSaved(){if(savedList==null)return;savedList.removeAllViews();JSONArray a=readSaved();if(a.length()==0){TextView empty=text("ماكو روابط محفوظة لحد الآن.",13,muted,false);savedList.addView(empty);return;}for(int i=a.length()-1;i>=0;i--){JSONObject o=a.optJSONObject(i);if(o==null)continue;final String name=o.optString("title","فيديو محفوظ"),url=o.optString("url","");LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(dp(13),dp(13),dp(13),dp(13));c.setBackground(shape(panel,14));LinearLayout.LayoutParams cpar=new LinearLayout.LayoutParams(-1,-2);cpar.bottomMargin=dp(10);savedList.addView(c,cpar);c.addView(text(name,15,white,true));TextView small=text(url,11,muted,false);small.setMaxLines(2);small.setTextIsSelectable(true);LinearLayout.LayoutParams smp=new LinearLayout.LayoutParams(-1,-2);smp.topMargin=dp(6);c.addView(small,smp);LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);c.addView(row);MaterialButton run=button("تشغيل",green,0xFF031914);row.addView(run,new LinearLayout.LayoutParams(0,dp(42),1));run.setOnClickListener(v->{input.setText(url);titleInput.setText(name);internal();});MaterialButton web=button("المتصفح",0xFF233649,white);LinearLayout.LayoutParams wp=new LinearLayout.LayoutParams(0,dp(42),1);wp.leftMargin=dp(5);row.addView(web,wp);web.setOnClickListener(v->{input.setText(url);titleInput.setText(name);makeBrowserLink();});MaterialButton del=button("حذف",0xFF552C38,white);LinearLayout.LayoutParams dpv=new LinearLayout.LayoutParams(0,dp(42),1);dpv.leftMargin=dp(5);row.addView(del,dpv);del.setOnClickListener(v->{deleteSaved(url);});}}
    void deleteSaved(String url){JSONArray old=readSaved(),next=new JSONArray();for(int i=0;i<old.length();i++){JSONObject o=old.optJSONObject(i);if(o!=null&&!url.equals(o.optString("url")))next.put(o);}getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(KEY,next.toString()).apply();renderSaved();status.setText("تم حذف الرابط المحفوظ.");}
    void paste(){android.content.ClipboardManager c=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(c!=null&&c.hasPrimaryClip()&&c.getPrimaryClip().getItemCount()>0){CharSequence v=c.getPrimaryClip().getItemAt(0).coerceToText(this);String u=extract(v==null?"":v.toString());if(u!=null){input.setText(u);status.setText("تم لصق الرابط.");}else status.setText("الحافظة لا تحتوي على رابط.");}else status.setText("الحافظة فارغة.");}
    void hideKeyboard(){((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(input.getWindowToken(),0);}
}