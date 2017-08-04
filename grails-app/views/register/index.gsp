<script type="text/javascript">
    function validateForm() {
        document.getElementById("userNameErrorMessage").innerHTML=null;
        document.getElementById("passwordErrorMessage").innerHTML=null;
        var uName = document.forms["register"]["username"].value;
        var validUserNameRegex = /^[0-9a-zA-Z@._]{3,128}$/;
        if (!validUserNameRegex.test(uName)) {
            document.getElementById("userNameErrorMessage").innerHTML = "Choose a user name between 3 to 128 characters. Allowed characters are letters, digits, '@', '_' and '.'";
            return false;
        }

        var validPasswordRegex = /.{6,12}/;
        var pwd = document.forms["register"]["password"].value;
        if (!validPasswordRegex.test(pwd)) {
            document.getElementById("passwordErrorMessage").innerHTML = "Choose a password between 6 to 128 characters.";
            return false;
        }
        return true;
    }
</script>

<fieldset>
    <legend>Register</legend>
    <g:form name="register" onsubmit="return validateForm()" action="create" method="POST">
        <g:if test="${params.error}">
            <p class="warning">
                ${params.error}
            </p>
        </g:if>
        <table class="information-box">
            <tbody>
            <tr>
                <td class="th">
                    <p><strong>Username:</strong></p>
                </td>
                <td>
                    <p><input type="text" name="username" value="" placeholder="username"
                              title="Choose a user name between 3 to 128 characters. Allowed characters are letters, digits, '@', '_' and '.'" required></p>
                </td>
                <td>
                    <span id="userNameErrorMessage" style="color:red"></span>
                </td>
            </tr>
            <tr>
                <td class="th">
                    <p><strong>Password:</strong></p>
                </td>
                <td>
                    <p><input type="password" name="password" value="" placeholder="password" title="Choose a password between 6 to 128 characters." required></p>
                </td>
                <td>
                    <span id="passwordErrorMessage" style="color:red"></span>
                </td>
            </tr>
            <tr>
                <td class="th">&nbsp;</td>
                <td>
                    <p><input type="submit" class="button" id="upload_button" value="Register"></p>
                </td>
            </tr>

            </tbody>
        </table>
    </g:form>
</fieldset>