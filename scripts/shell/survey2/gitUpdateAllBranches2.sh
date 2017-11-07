RED=`tput setaf 1`
RESET=`tput sgr0`
PATH_PROJECT="/home/aline/utilitarios/workspaces/datasetBreakingChanges/survey_II_whyWeBreakAPIs_projects_bibliotecas_sem_javadoc"

clear;

echo "Process path $PATH_PROJECT"
count=1;
while read line 
do  
	echo ""
	echo ""	
	echo "###################### PROJECT $count -  $line ######################"
	PATH_CURRENT=$PATH_PROJECT"/"$line
	if [ -d "$PATH_CURRENT" ]; then
		cd $PATH_CURRENT
	  for branch in `git branch -r | sed -r 's/^.{9}//' | grep -v HEAD` ; do 
			echo ""
			echo "-----"
			echo "CHECKOUT $branch"
			git checkout $branch
			git reset --hard "origin/"$branch ## reset modificações na branch.
			git pull origin $branch;
		done
	else
		echo "${RED}Path doesn't exist! [$PATH_CURRENT]${RESET}"
	fi
	count=$((count+1));
done < $1

cd $PATH_PROJECT

