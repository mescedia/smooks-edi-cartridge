/*-
 * ========================LICENSE_START=================================
 * smooks-ect
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.javabean.converter;

import org.smooks.converter.TypeConverter;
import org.smooks.converter.TypeConverterDescriptor;
import org.smooks.converter.TypeConverterException;
import org.smooks.converter.factory.TypeConverterFactory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * {@link BigDecimal} Decoder, which is EDI delimiters aware for parsing decimal.
 * 
 * @author <a href="mailto:sinfomicien@gmail.com">sinfomicien@gmail.com</a>
 * @author <a href="mailto:michael@krueske.net">michael@krueske.net</a> (patched to ensure that always a {@link BigDecimal} value is decoded)
 */

public class StringToDABigDecimalTypeConverterFactory implements TypeConverterFactory<String, BigDecimal> {
    
    @Override
    public TypeConverter<String, BigDecimal> createTypeConverter() {
        return new DABigDecimalTypeConverter<String, BigDecimal>() {
            @Override
            protected BigDecimal doConvert(String value) {
                DecimalFormat decimalFormat = getDecimalFormat();
                setDecimalPointFormat(decimalFormat, getContextDelimiters());
                final Number number;
                try {
                    number = decimalFormat.parse(value.trim());
                } catch (final ParseException e) {
                    throw new TypeConverterException("Failed to decode BigDecimal value '" + value
                            + "' using NumberFormat instance " + decimalFormat + ".", e);
                }
                
                return (BigDecimal) number;
            }
        };
    }

    @Override
    public TypeConverterDescriptor<Class<String>, Class<BigDecimal>> getTypeConverterDescriptor() {
        return new TypeConverterDescriptor<>(String.class, BigDecimal.class);
    }
}
