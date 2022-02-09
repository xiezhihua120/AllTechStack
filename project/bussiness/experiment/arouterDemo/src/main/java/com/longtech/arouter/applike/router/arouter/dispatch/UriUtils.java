package com.longtech.arouter.applike.router.arouter.dispatch;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.luojilab.router.facade.enums.Type;

import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by mrzhang on 2017/8/28.
 * 负责解析URI的参数
 */

public class UriUtils {

    public static HashMap<String, String> parseParams(Uri uri) {
        if (uri == null) {
            return new HashMap<String, String>();
        }
        HashMap<String, String> temp = new HashMap<String, String>();
        Set<String> keys = getQueryParameterNames(uri);
        for (String key : keys) {
            temp.put(key, uri.getQueryParameter(key));
        }
        return temp;
    }

    public static Set<String> getQueryParameterNames(Uri uri) {
        String query = uri.getEncodedQuery();
        if (query == null) {
            return Collections.emptySet();
        }

        Set<String> names = new LinkedHashSet<String>();
        int start = 0;
        do {
            int next = query.indexOf('&', start);
            int end = (next == -1) ? query.length() : next;

            int separator = query.indexOf('=', start);
            if (separator > end || separator == -1) {
                separator = end;
            }

            String name = query.substring(start, separator);
            try {
                names.add(URLDecoder.decode(name, "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            start = end + 1;
        } while (start < query.length());

        return Collections.unmodifiableSet(names);
    }
}
