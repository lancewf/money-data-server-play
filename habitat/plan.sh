pkg_name=money_data_server
pkg_origin=lancewf
pkg_version="0.1.0"
pkg_maintainer="Lance Finfrock <lancewf@gmail.com>"
pkg_license=("Apache-2.0")
pkg_source=""
pkg_deps=(core/jdk8 lancewf/sbt core/mysql-client)

pkg_svc_user=root
pkg_svc_group=$pkg_svc_user

pkg_exports=(
   [port]=http.listen.port
   [local_only]=http.listen.local_only
)

pkg_binds=(
  [database]="port username password local_only"
)

do_build(){
  sbt clean stage

  return 0
}

do_install() {
  mkdir -p $pkg_prefix/bin
  cp -r target/universal/stage/bin/* $pkg_prefix/bin/.
  mkdir -p $pkg_prefix/conf
  cp -r target/universal/stage/conf/* $pkg_prefix/conf/.
  mkdir -p $pkg_prefix/lib
  cp -r target/universal/stage/lib/* $pkg_prefix/lib/.
  mkdir -p $pkg_prefix/share
  cp -r target/universal/stage/share/* $pkg_prefix/share/.
  
  if [[ ! -r /usr/bin/env ]]; then
    ln -sv "$(pkg_path_for coreutils)/bin/env" /usr/bin/env
  fi

  return 0
}
