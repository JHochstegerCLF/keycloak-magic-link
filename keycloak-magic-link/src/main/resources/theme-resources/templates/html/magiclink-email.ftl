<#import "template.ftl" as layout>
<@layout.emailLayout>
    ${kcSanitize(msg("magiclink-emailBodyHtml", link))?no_esc}
</@layout.emailLayout>