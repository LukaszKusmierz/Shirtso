package org.peter_lukas.shirtso.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class LocalDateTimeToDateAdapter  implements DateAdapter {

    @Override
    public Date convertToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
