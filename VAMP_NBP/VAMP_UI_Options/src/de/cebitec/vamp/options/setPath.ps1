Param([Parameter(Mandatory=$true)][string]$Directory)
$wsh = new-object -com wscript.shell;
$path = $wsh.Environment("User").Item("Path");
if ($path) {$path += ";"+$Directory} else {$path = $Directory}
$wsh.Environment("User").Item("Path") = $path;