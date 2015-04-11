#!/bin/sh

check_headers () {
	EXPECT=" * Copyright (c) "
	LEXPECT=${#EXPECT}

	for file in $(find . -type f -name "*.java"); do
		line2=$(head -n 2 "${file}" | tail -n 1)
		l2start=$(expr substr "$line2" 1 ${LEXPECT})
		if [ "${EXPECT}" != "${l2start}" ]; then
			echo "no copyright notice in: $file"
		fi
	done
}

check_updates () {
	mvn -U versions:display-dependency-updates versions:display-plugin-updates versions:display-property-updates
}

check_coverage() {
	mvn cobertura:cobertura
}

check_coverage_xml() {
	mvn cobertura:cobertura -Dcobertura.report.format=xml
}

case $1 in
	headers)
		check_headers
		;;
	updates)
		check_updates
		;;
	coverage)
		check_coverage
		;;
	coverage_xml)
		check_coverage_xml
		;;
	all)
		check_headers
		check_updates
		check_coverage
		;;
	*)
		echo "$0 <which check>"
		echo "checks:"
		echo "  all"
		echo "  headers"
		echo "  updates"
		echo "  coverage"
		echo "  coverage_xml"
		;;
esac
