/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2020-2022, Ondrej Fischer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package foundation.jpa.querydsl.parsers;

import foundation.jpa.querydsl.parsers.order.OrderSpecifierParser;
import foundation.rpg.parser.SyntaxError;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;

public class OrderSpecifierParserTest {


    private final OrderSpecifierParser parser = new OrderSpecifierParser(mock(OrderRules.class));

    @DataProvider
    public Object[][] validPredicates() {
        return new Object[][] {
                {"field > 3"},
                {"field = 3"},
                {"field != 3"},
                {"3 = field"},
                {"3 = 3"},
                {"field1 = field2"},
                {"field asc"},
                {"field desc"},
                {"field1 asc, field2 desc"},
                {"field desc, 3 + 4"},
                {"field nulls first"},
                {"field nulls last"},
                {"field desc nulls first"},
                {"field asc nulls last"},
                {"field desc nulls first"},
                {"field asc nulls last"},
        };
    }

    @Test(dataProvider = "validPredicates")
    public void predicateParserShouldAccept(String predicate) throws IOException {
        parser.parseString(predicate);
    }

    @DataProvider
    public Object[][] invalidPredicates() {
        return new Object[][] {
                {"field 3"},
                {"field1 asc asc"}
        };
    }

    @Test(dataProvider = "invalidPredicates", expectedExceptions = SyntaxError.class)
    public void predicateParserShouldReject(String predicate) throws IOException {
        parser.parseString(predicate);
    }

}
