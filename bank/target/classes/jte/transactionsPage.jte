@import edu.cwi.UserAccount
@import edu.cwi.Account
@import java.util.List

@param String title
@param UserAccount account
@param List<Account> accounts
@param Long accountId
@param String csrfToken

@template.accountPage(title = title, account = account, accounts = accounts,
    accountId = accountId, csrfToken = csrfToken)
