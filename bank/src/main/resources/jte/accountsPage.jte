@import edu.cwi.Account
@import java.util.List

@param String name
@param List<Account> accounts
@param String csrfToken

@template.accountLayout(
    title = "My Accounts",
    csrfToken = csrfToken,
    account = @`
        <h2>${name}</h2>
        <p>User Accounts</p>
    `,
    detail = @`
        @if(accounts.isEmpty())
            <p>You do not have a bank account yet.</p>
        @else
            <table>
                <thead>
                    <tr>
                        <th>Account</th>
                        <th>Balance</th>
                        <th>Details</th>
                    </tr>
                </thead>
                <tbody>
                    @for(Account item : accounts)
                        <tr>
                            <td>${item.getId()}</td>
                            <td>$${item.getBalance().toPlainString()}</td>
                            <td><a href="/account/${item.getId()}">Detail</a></td>
                        </tr>
                    @endfor
                </tbody>
            </table>
        @endif
    `
)