package org.peter_lukas.shirtso.utils;

import java.time.LocalDateTime;
import java.util.Date;

public interface DateAdapter {
    Date convertToDate(LocalDateTime localDateTime);
}
