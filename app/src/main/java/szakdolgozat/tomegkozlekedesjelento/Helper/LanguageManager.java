package szakdolgozat.tomegkozlekedesjelento.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class LanguageManager
{

    private static final String PREFS_NAME = "language_prefs";
    private static final String KEY_LANGUAGE = "language";
    private SharedPreferences sharedPreferences;
    private Context context;

    public LanguageManager(Context context)
    {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public Context updateResource(String code) {
        Locale locale = new Locale(code);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        context = context.createConfigurationContext(config);
        setLanguage(code);
        return context;
    }


    public String getLang()
    {
        return sharedPreferences.getString("lang","hu");
    }
    public void setLanguage(String code)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lang",code);
        editor.apply();
    }
}
