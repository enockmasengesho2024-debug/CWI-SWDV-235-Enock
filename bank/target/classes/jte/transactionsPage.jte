@import java.util.List
@import edu.cwi.TransactionsController.TransactionData
@param List<TransactionData> transactions

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Transactions</title>
</head>
<body>
    <h1>Transactions</h1>

    <h2>Add transaction</h2>
    <form method="post" action="/transactions">
        <p>
            <label for="description">Description</label><br>
            <input id="description" name="description" type="text"
                   maxlength="120" required>
        </p>
        <p>
            <label for="amount">Amount</label><br>
            <input id="amount" name="amount" type="number"
                   step="0.01" required>
        </p>
        <button type="submit">Save transaction</button>
    </form>

    <h2>Saved transactions</h2>
    @if(transactions.isEmpty())
        <p>No transactions yet.</p>
    @endif

    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Description</th>
                <th>Amount</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
            @for(TransactionData transaction : transactions)
                <tr>
                    <td>${transaction.id()}</td>
                    <td>${transaction.description()}</td>
                    <td>${transaction.amount().toPlainString()}</td>
                    <td>
                        <a href="/transactions/${transaction.id()}">View / edit</a>
                        <form method="post"
                              action="/transactions/${transaction.id()}/delete">
                            <button type="submit">Delete</button>
                        </form>
                    </td>
                </tr>
            @endfor
        </tbody>
    </table>
</body>
</html>