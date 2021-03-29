DESCRIPTION = "Platform Loader and Manager"
SUMMARY = "Platform Loader and Manager for Versal devices"

LICENSE = "MIT"

PROVIDES = "virtual/plm"

INHERIT_DEFAULT_DEPENDS = "1"

COMPATIBLE_MACHINE = "^$"
COMPATIBLE_MACHINE_versal = "versal"

# This is really BOARD specific, but we'll set it MACHINE for now.
PACKAGE_ARCH = "${MACHINE_ARCH}"

# Default would be a multiconfig (versal) build
# For this to work, BBMULTICONFIG += "versal-fw" must be in the user's local.conf!
PLM_DEPENDS ??= ""
PLM_MCDEPENDS ??= "mc::versal-fw:plm-firmware:do_deploy"

# This must be defined to the file output by whatever is providing the plm-firmware
# The following sets the default, but the BSP may select a different name
PLM_IMAGE_NAME ??= "plm-versal-mb"
PLM_DEPLOY_DIR ??= "${TOPDIR}/tmp-microblaze-versal-fw/deploy/images/${MACHINE}"

# Default is for the multilib case (without the extension .elf/.bin)
PLM_FILE ??= "${PLM_DEPLOY_DIR}/${PLM_IMAGE_NAME}"

do_fetch[depends] += "${PLM_DEPENDS}"
do_fetch[mcdepends] += "${PLM_MCDEPENDS}"

inherit update-alternatives deploy

BINARY_NAME = "${PN}"
BINARY_EXT = ".elf"
BINARY_ID = "${@d.getVar('SRCPV') if d.getVar('SRCPV') else d.getVar('PR') }"

do_install() {
    if [ ! -e ${PLM_FILE}${BINARY_EXT} ]; then
        echo "Unable to find PLM_FILE (${PLM_FILE}${BINARY_EXT})"
        exit 1
    fi

    install -Dm 0644 ${PLM_FILE}${BINARY_EXT} ${D}/boot/${BINARY_NAME}-${BINARY_ID}${BINARY_EXT}
}

# If the item is already in OUR deploy_image_dir, nothing to deploy!
SHOULD_DEPLOY = "${@'false' if (d.getVar('PLM_FILE')).startswith(d.getVar('DEPLOY_DIR_IMAGE')) else 'true'}"
do_deploy() {
    # If the item is already in OUR deploy_image_dir, nothing to deploy!
    if ${SHOULD_DEPLOY}; then
        install -Dm 0644 ${PLM_FILE}.elf ${DEPLOYDIR}/${PLM_IMAGE_NAME}.elf
        install -Dm 0644 ${PLM_FILE}.bin ${DEPLOYDIR}/${PLM_IMAGE_NAME}.bin
    fi
}

addtask deploy before do_build after do_install

ALTERNATIVE_${PN} = "plmfw"
ALTERNATIVE_TARGET[plmfw] = "/boot/${BINARY_NAME}-${BINARY_ID}${BINARY_EXT}"
ALTERNATIVE_LINK_NAME[plmfw] = "/boot/${BINARY_NAME}${BINARY_EXT}"

INSANE_SKIP_${PN} = "arch"
INSANE_SKIP_${PN}-dbg = "arch"

SYSROOT_DIRS += "/boot"
FILES_${PN} = "/boot/${BINARY_NAME}-${BINARY_ID}${BINARY_EXT}"
