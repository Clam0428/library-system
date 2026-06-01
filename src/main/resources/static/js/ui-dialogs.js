(function () {
    if (window.ui && window.ui.toast && window.ui.confirm) return;

    var dialogQueue = Promise.resolve();

    function ensureDom() {
        if (!document.getElementById('uiDialogModal')) {
            var wrapper = document.createElement('div');
            wrapper.innerHTML = '' +
                '<div class="modal fade" id="uiDialogModal" tabindex="-1" role="dialog" aria-hidden="true">' +
                '  <div class="modal-dialog modal-dialog-centered modal-sm" role="document">' +
                '    <div class="modal-content">' +
                '      <div class="modal-header">' +
                '        <div>' +
                '          <div class="modal-title" id="uiDialogTitle">提示</div>' +
                '          <div class="modal-subtitle" id="uiDialogSubtitle" style="display:none;font-size:12px;color:rgba(17,24,39,0.55);margin-top:2px;"></div>' +
                '        </div>' +
                '        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
                '      </div>' +
                '      <div class="modal-body">' +
                '        <div id="uiDialogMessage" style="white-space:pre-wrap;color:rgba(17,24,39,0.86);"></div>' +
                '        <div id="uiDialogInputWrap" style="display:none;margin-top:14px;">' +
                '          <input id="uiDialogInput" class="form-control" />' +
                '        </div>' +
                '      </div>' +
                '      <div class="modal-footer">' +
                '        <button type="button" class="btn btn-secondary" id="uiDialogCancelBtn" data-dismiss="modal">取消</button>' +
                '        <button type="button" class="btn btn-primary" id="uiDialogOkBtn">确定</button>' +
                '      </div>' +
                '    </div>' +
                '  </div>' +
                '</div>';
            document.body.appendChild(wrapper.firstChild);
        }
    }

    function showDialog(opts) {
        ensureDom();
        var $m = $('#uiDialogModal');
        var $title = $('#uiDialogTitle');
        var $sub = $('#uiDialogSubtitle');
        var $msg = $('#uiDialogMessage');
        var $ok = $('#uiDialogOkBtn');
        var $cancel = $('#uiDialogCancelBtn');
        var $inputWrap = $('#uiDialogInputWrap');
        var $input = $('#uiDialogInput');

        $title.text(opts.title || '提示');
        if (opts.subtitle) {
            $sub.text(opts.subtitle).show();
        } else {
            $sub.hide();
        }
        $msg.text(opts.message || '');

        if (opts.input) {
            $inputWrap.show();
            $input.val(opts.input.value || '');
            $input.attr('type', opts.input.type || 'text');
            if (opts.input.placeholder) $input.attr('placeholder', opts.input.placeholder);
        } else {
            $inputWrap.hide();
            $input.val('');
        }

        if (opts.okText) $ok.text(opts.okText);
        else $ok.text('确定');
        if (opts.cancelText) $cancel.text(opts.cancelText);
        else $cancel.text('取消');

        if (opts.showCancel === false) {
            $cancel.hide();
        } else {
            $cancel.show();
        }

        return new Promise(function (resolve) {
            var resolved = false;
            function cleanup() {
                $ok.off('click.uiDialog');
                $m.off('hidden.bs.modal.uiDialog');
                $input.off('keydown.uiDialog');
            }

            function done(val) {
                if (resolved) return;
                resolved = true;
                cleanup();
                resolve(val);
            }

            $ok.off('click.uiDialog').on('click.uiDialog', function () {
                if (opts.input) {
                    done($input.val());
                } else {
                    done(true);
                }
                $m.modal('hide');
            });

            $m.off('hidden.bs.modal.uiDialog').on('hidden.bs.modal.uiDialog', function () {
                if (!resolved) {
                    if (opts.input) done(null);
                    else done(false);
                }
            });

            if (opts.input) {
                $input.off('keydown.uiDialog').on('keydown.uiDialog', function (e) {
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        $ok.trigger('click');
                    }
                });
            }

            $m.modal('show');
            if (opts.input) {
                window.setTimeout(function () {
                    $input.trigger('focus');
                    var v = $input.val() || '';
                    $input[0].setSelectionRange(v.length, v.length);
                }, 120);
            }
        });
    }

    function enqueueDialog(opts) {
        dialogQueue = dialogQueue.then(function () {
            return showDialog(opts);
        }).catch(function () {
            return showDialog(opts);
        });
        return dialogQueue;
    }

    window.ui = {
        toast: function (message, type) {
            var t = String(type || '').toLowerCase();
            var title = '提示';
            if (t === 'success') title = '成功';
            if (t === 'warning') title = '提示';
            if (t === 'danger' || t === 'error') title = '操作失败';
            if (t === 'info') title = '提示';
            return enqueueDialog({
                title: title,
                message: message || '',
                okText: '确定',
                showCancel: false
            }).then(function () { });
        },
        alert: function (message, title) {
            return enqueueDialog({
                title: title || '提示',
                message: message || '',
                okText: '确定',
                showCancel: false
            }).then(function () { });
        },
        confirm: function (message, title, okText, cancelText) {
            return enqueueDialog({
                title: title || '确认操作',
                message: message || '',
                okText: okText || '确定',
                cancelText: cancelText || '取消',
                showCancel: true
            }).then(function (v) { return v === true; });
        },
        prompt: function (message, title, defaultValue, placeholder) {
            return enqueueDialog({
                title: title || '请输入',
                message: message || '',
                okText: '确定',
                cancelText: '取消',
                showCancel: true,
                input: {
                    value: defaultValue == null ? '' : String(defaultValue),
                    placeholder: placeholder || ''
                }
            });
        }
    };

    window.alert = function (message) {
        window.ui.alert(message || '');
    };
})();
