package com.aplana.timesheet.form.validator;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * <pre>
 *  <table border="1">
 *      <tr>
 *          <th>Автор</th>
 *          <th>Компания</th>
 *          <th>Описание изменения</th>
 *          <th>Дата изменения</th>
 *      </tr>
 *      <tr>
 *          <td>iziyangirov</td>
 *          <td>&nbsp;</td>
 *          <td>Для тестов необходим request вот и возвращается</td>
 *          <td>17.01.13</td>
 *      </tr>
 *  </table>
 * </pre>
 */

public class FakeRequestAttributes {
    public HttpServletRequest getHttpRequest(){
        return null;
    }
}