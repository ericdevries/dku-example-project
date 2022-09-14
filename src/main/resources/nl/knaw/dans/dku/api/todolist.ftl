<#-- @ftlvariable name="" type="nl.knaw.dans.dku.api.OverdueTodosView" -->
<html>
    <body>
        <h1>Overdue TODO's</h1>

        <ul>
            <#list todoItems as item>
                <li>${item.title} - (due ${item.dueDate})</li>
            </#list>
        </ul>

    </body>
</html>