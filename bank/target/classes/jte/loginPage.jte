@param String error
@param String username
@param String csrfToken

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CWI Bank - Log In</title>
</head>
<body>
    <h1>CWI Bank</h1>
    <h2>Log In</h2>

    @if(!error.isEmpty())
        <p role="alert">${error}</p>
    @endif

    <form action="/login" method="post">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <p>
            <label for="username">Username</label>
            <input id="username" name="username" type="text"
                   value="${username}" autocomplete="username" required>
        </p>
        <p>
            <label for="password">Password</label>
            <input id="password" name="password" type="password"
                   maxlength="200" autocomplete="current-password" required>
        </p>
        <button type="submit">Log In</button>
    </form>
</body>
</html>
