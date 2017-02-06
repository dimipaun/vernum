
VERSION := $(shell grep '^version *=' build.gradle | sed "s/.*= *['\"]\(.*\)['\"]/\1/")

info:
	@echo "VERSION = $(VERSION)"

release:
	./gradlew uploadArchives closeAndPromoteRepository
	git tag $(VERSION)
	git push --tags

