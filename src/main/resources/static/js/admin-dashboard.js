/* admin-dashboard.js - initializes charts and loads sample data */
$(function(){
    // 忽略因页面切换导致的已发起请求中断（abort）的报错，避免控制台噪音
    $(document).ajaxError(function(event, jqxhr){
        if (jqxhr && jqxhr.statusText === 'abort') {
            return;
        }
    });

    window.safeShowModal = function(selector){
        var $m = $(selector);
        if(!$m.length){ return; }
        try{
            if($m.parent()[0] !== document.body){ $m.appendTo(document.body); }
            $m.attr('aria-hidden','false').attr('tabindex','-1');
            if($.fn && $.fn.modal){ $m.modal('show'); }
            else {
                $m.addClass('show').css('display','block');
                $('body').addClass('modal-open');
                if($('.modal-backdrop').length===0){ $('body').append('<div class="modal-backdrop fade show"></div>'); }
            }
            $m.focus();
        } catch(e){
            if($m.parent()[0] !== document.body){ $m.appendTo(document.body); }
            $m.addClass('show').css('display','block');
            if($('.modal-backdrop').length===0){ $('body').append('<div class="modal-backdrop fade show"></div>'); }
        }
    };
    window.safeHideModal = function(selector){
        var $m = $(selector);
        try{
            if($.fn && $.fn.modal){ $m.modal('hide'); }
            else {
                $m.attr('aria-hidden','true');
                $m.removeClass('show').css('display','none');
                $('body').removeClass('modal-open');
                $('.modal-backdrop').remove();
            }
        } catch(e){
            $m.removeClass('show').css('display','none');
            $('body').removeClass('modal-open');
            $('.modal-backdrop').remove();
        }
    };

    function ensureLendDetailModal(){
        if($('#globalLendDetailModal').length){ return; }
        var html = ''+
            '<div class="modal fade" id="globalLendDetailModal" tabindex="-1" role="dialog" aria-hidden="true">'+
            '<div class="modal-dialog modal-lg modal-dialog-centered" role="document">'+
            '<div class="modal-content border-0 shadow-lg" style="border-radius: 28px; overflow: hidden;">'+
            '<div class="modal-header bg-primary text-white border-0">'+
            '<h5 class="modal-title"><i class="fas fa-info-circle mr-2"></i>借阅详情</h5>'+
            '<button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">'+
            '<span aria-hidden="true">&times;</span>'+
            '</button>'+
            '</div>'+
            '<div class="modal-body p-4">'+
            '<div class="row mb-4">'+
            '<div class="col-12"><h6 class="text-primary border-bottom pb-2 mb-3"><i class="fas fa-user-circle mr-2"></i>借阅人信息</h6></div>'+
            '<div class="col-md-4 mb-3"><label class="small text-muted">姓名</label><input type="text" id="gReader" class="form-control-plaintext font-weight-bold" readonly></div>'+
            '<div class="col-md-4 mb-3"><label class="small text-muted">学号</label><input type="text" id="gReaderNo" class="form-control-plaintext font-weight-bold" readonly></div>'+
            '<div class="col-md-4 mb-3"><label class="small text-muted">图书</label><input type="text" id="gBook" class="form-control-plaintext font-weight-bold" readonly></div>'+
            '<div class="col-md-4 mb-3"><label class="small text-muted">院系</label><input type="text" id="gDept" class="form-control-plaintext font-weight-bold" readonly></div>'+
            '<div class="col-md-4 mb-3"><label class="small text-muted">手机号</label><input type="text" id="gPhone" class="form-control-plaintext font-weight-bold" readonly></div>'+
            '<div class="col-md-4 mb-3"><label class="small text-muted">状态</label><div id="gStatus" class="font-weight-bold"></div></div>'+
            '</div>'+
            '<div class="row mb-4">'+
            '<div class="col-12"><h6 class="text-primary border-bottom pb-2 mb-3"><i class="fas fa-calendar-alt mr-2"></i>借阅信息</h6></div>'+
            '<div class="col-md-4 mb-3"><label class="small text-muted">借阅时间</label><input type="text" id="gLendDate" class="form-control-plaintext font-weight-bold" readonly></div>'+
            '<div class="col-md-4 mb-3"><label class="small text-muted">实际归还时间</label><input type="text" id="gBackDate" class="form-control-plaintext font-weight-bold" readonly></div>'+
            '<div class="col-md-4 mb-3"><label class="small text-muted">逾期天数</label><input type="text" id="gOverdueDays" class="form-control-plaintext font-weight-bold" readonly></div>'+
            '<div class="col-md-4 mb-3"><label class="small text-muted">罚款金额</label><div id="gFine" class="font-weight-bold"></div></div>'+
            '<div class="col-md-8 mb-3"><label class="small text-muted">借期信息</label><div id="gApplyInline" class="font-weight-bold"></div></div>'+
            '</div>'+
            '<div class="row" id="gApplyGroup" style="display:none;">'+
            '<div class="col-12">'+
            '<div class="p-3 bg-light rounded border-left border-warning">'+
            '<div class="d-flex align-items-center">'+
            '<i class="fas fa-book-reader fa-2x text-warning mr-3"></i>'+
            '<div><div class="font-weight-bold">审核信息</div><div id="gAudit" class="text-secondary"></div></div>'+
            '</div></div></div></div>'+
            '</div>'+
            '<div class="modal-footer border-0">'+
            '<button type="button" class="btn btn-light" data-dismiss="modal">关闭</button>'+
            '</div></div></div></div>';
        $('body').append(html);
    }

    function ensureLendReturnModal(){
        if($('#globalLendReturnModal').length){ return; }
        var html = ''+
            '<div class="modal fade" id="globalLendReturnModal" tabindex="-1" role="dialog" aria-hidden="true">'+
            '<div class="modal-dialog modal-lg modal-dialog-centered" role="document">'+
            '<div class="modal-content border-0 shadow-lg" style="border-radius: 28px; overflow: hidden;">'+
            '<div class="modal-header bg-success text-white border-0">'+
            '<h5 class="modal-title"><i class="fas fa-undo mr-2"></i>标记归还</h5>'+
            '<button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">'+
            '<span aria-hidden="true">&times;</span>'+
            '</button>'+
            '</div>'+
            '<div class="modal-body p-4">'+
            '<input type="hidden" id="gReturnLendId"/>'+
            '<div class="row mb-4">'+
            '<div class="col-12"><h6 class="text-success border-bottom pb-2 mb-3"><i class="fas fa-clipboard-check mr-2"></i>归还确认</h6></div>'+
            '<div class="col-12">'+
            '<div class="custom-control custom-checkbox mb-3">'+
            '<input type="checkbox" class="custom-control-input" id="gDamageFlag">'+
            '<label class="custom-control-label" for="gDamageFlag">图书损坏 / 丢失</label>'+
            '</div>'+
            '<div class="form-group" id="gDamageGroup" style="display:none;">'+
            '<label class="small text-muted font-weight-bold">说明</label>'+
            '<textarea class="form-control shadow-sm" rows="3" id="gDamageRemarks" style="border-radius:10px;" placeholder="请填写损坏/丢失说明（可选）"></textarea>'+
            '</div>'+
            '<small class="text-muted d-block">逾期信息将自动计算并记录，无需手动选择。</small>'+
            '</div></div>'+
            '</div>'+
            '<div class="modal-footer border-0">'+
            '<button type="button" class="btn btn-light" data-dismiss="modal">取消</button>'+
            '<button type="button" class="btn btn-success px-4" id="gConfirmReturn">确认</button>'+
            '</div></div></div></div>';
        $('body').append(html);
        $(document).on('change', '#gDamageFlag', function(){
            $('#gDamageGroup').toggle(this.checked);
        });
        $(document).on('click', '#gConfirmReturn', function(){
            var lendId = $('#gReturnLendId').val();
            if(!lendId){ return; }
            var damage = $('#gDamageFlag').is(':checked');
            var remarks = $('#gDamageRemarks').val() || '';
            var $btn = $(this);
            $btn.prop('disabled', true).text('处理中...');
            if(damage){
                $.post('/api/lend/reportDamage/' + lendId, {remarks: remarks}).done(function(resp){
                    if(resp && resp.code===0){
                        safeHideModal('#globalLendReturnModal');
                        $('#fragmentLends #lendsFilterForm').trigger('submit');
                    } else {
                        if(window.ui && window.ui.toast) window.ui.toast((resp&&resp.msg)||'操作失败', 'danger');
                    }
                }).fail(function(){ if(window.ui && window.ui.toast) window.ui.toast('请求失败', 'danger'); }).always(function(){ $btn.prop('disabled', false).text('确认'); });
            } else {
                $.post('/api/lend/return/' + lendId).done(function(resp){
                    if(resp && resp.code===0){
                        safeHideModal('#globalLendReturnModal');
                        $('#fragmentLends #lendsFilterForm').trigger('submit');
                    } else {
                        if(window.ui && window.ui.toast) window.ui.toast((resp&&resp.msg)||'操作失败', 'danger');
                    }
                }).fail(function(){ if(window.ui && window.ui.toast) window.ui.toast('请求失败', 'danger'); }).always(function(){ $btn.prop('disabled', false).text('确认'); });
            }
        });
    }

    var currentAdmin = null;

    function sexText(v){
        var s = parseInt(v);
        if(s === 1) return '男';
        if(s === 2) return '女';
        return '男';
    }

    function fillAdminProfile(admin){
        currentAdmin = admin || null;
        var name = (admin && (admin.realName || admin.username)) || '管理员';
        $('#adminProfileName').text(name);

        $('#adminRealName').val((admin && admin.realName) || '');
        $('#adminSex').val((admin && admin.sex !== undefined && admin.sex !== null) ? String(admin.sex) : '1');
        $('#adminJobNumber').val((admin && admin.jobNumber) || '');
        $('#adminTel').val((admin && admin.tel) || '');
        $('#adminUsername').val((admin && admin.username) || '');
    }

    function loadCurrentAdmin(){
        return $.get('/api/admin/currentUser').done(function(resp){
            if(resp && resp.code === 0 && resp.data){
                fillAdminProfile(resp.data);
            }
        });
    }

    loadCurrentAdmin();

    $('#adminProfileEntry').on('click', function(){
        loadCurrentAdmin().always(function(){
            $('#adminOldPwd').val('');
            $('#adminNewPwd').val('');
            $('#adminNewPwd2').val('');
            if(window.safeShowModal) window.safeShowModal('#adminProfileModal');
            else $('#adminProfileModal').modal('show');
        });
    });

    $('#adminProfileForm').on('submit', function(e){
        e.preventDefault();
        var payload = {
            realName: ($('#adminRealName').val() || '').trim(),
            sex: parseInt($('#adminSex').val() || '0'),
            jobNumber: ($('#adminJobNumber').val() || '').trim(),
            tel: ($('#adminTel').val() || '').trim(),
            username: ($('#adminUsername').val() || '').trim()
        };
        $('#btnSaveAdminProfile').prop('disabled', true).text('保存中...');
        $.ajax({
            url: '/api/admin/updateProfile',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(payload)
        }).done(function(resp){
            if(resp && resp.code === 0 && resp.data){
                fillAdminProfile(resp.data);
                if(window.ui) window.ui.toast('资料已保存', 'success');
            } else {
                if(window.ui) window.ui.alert((resp && (resp.msg || resp.message)) || '保存失败', '提示');
            }
        }).fail(function(){
            if(window.ui) window.ui.alert('保存失败，请稍后重试', '提示');
        }).always(function(){
            $('#btnSaveAdminProfile').prop('disabled', false).text('保存资料');
        });
    });

    $('#btnSaveAdminPwd').on('click', function(){
        if(!currentAdmin || !currentAdmin.id){
            if(window.ui) window.ui.alert('无法获取管理员信息，请重新登录', '提示');
            return;
        }
        var oldPwd = $('#adminOldPwd').val() || '';
        var newPwd = $('#adminNewPwd').val() || '';
        var newPwd2 = $('#adminNewPwd2').val() || '';
        if(!oldPwd || !newPwd){
            if(window.ui) window.ui.alert('请填写原密码与新密码', '提示');
            return;
        }
        if(newPwd !== newPwd2){
            if(window.ui) window.ui.alert('两次输入的新密码不一致', '提示');
            return;
        }
        $('#btnSaveAdminPwd').prop('disabled', true).text('更新中...');
        $.post('/api/admin/updatePassword', {
            id: currentAdmin.id,
            oldPassword: oldPwd,
            newPassword: newPwd
        }).done(function(resp){
            if(resp && resp.code === 0){
                $('#adminOldPwd').val('');
                $('#adminNewPwd').val('');
                $('#adminNewPwd2').val('');
                if(window.ui) window.ui.toast('密码已更新', 'success');
            } else {
                if(window.ui) window.ui.alert((resp && (resp.msg || resp.message)) || '更新失败', '提示');
            }
        }).fail(function(){
            if(window.ui) window.ui.alert('更新失败，请稍后重试', '提示');
        }).always(function(){
            $('#btnSaveAdminPwd').prop('disabled', false).text('更新密码');
        });
    });

    $('#logoutBtn').click(function(){
        $.post('/api/admin/logout').always(function(){
            window.location.href = '/login';
        });
    });

    // Chart instances (kept to allow destroy/recreate on fragment navigation)
    var borrowTrendChart = null;
    var typePieChart = null;
    var genderChart = null;
    var departmentChart = null;

    function initDashboardCharts(){
        // destroy existing charts if present
        try{ if(borrowTrendChart){ borrowTrendChart.destroy(); borrowTrendChart = null; } }catch(e){ console.warn(e); }
        try{ if(typePieChart){ typePieChart.destroy(); typePieChart = null; } }catch(e){ console.warn(e); }
        try{ if(genderChart){ genderChart.destroy(); genderChart = null; } }catch(e){ console.warn(e); }
        try{ if(departmentChart){ departmentChart.destroy(); departmentChart = null; } }catch(e){ console.warn(e); }

        var borrowCanvas = document.getElementById('borrowTrendChart');
        var typeCanvas = document.getElementById('typePieChart');
        var genderCanvas = document.getElementById('genderChart');
        var deptCanvas = document.getElementById('departmentChart');

        if(borrowCanvas){
            var borrowCtx = borrowCanvas.getContext('2d');
            borrowTrendChart = new Chart(borrowCtx, {
                type: 'line',
                data: {
                    labels: [],
                    datasets: [{
                        label: '借阅量',
                        data: [],
                        backgroundColor: 'rgba(54,162,235,0.12)',
                        borderColor: 'rgba(54,162,235,1)',
                        fill: true,
                        pointRadius: 3,
                        tension: 0.4
                    }]
                },
                options: { responsive:true, maintainAspectRatio:true, aspectRatio: 2 }
            });
        }

        if(typeCanvas){
            var typeCtx = typeCanvas.getContext('2d');
            typePieChart = new Chart(typeCtx, {
                type: 'doughnut',
                data: {
                    labels: [],
                    datasets: [{
                        data: [],
                        backgroundColor: ['#007bff','#28a745','#ffc107','#17a2b8','#6c757d','#e83e8c','#fd7e14']
                    }]
                },
                options: { responsive:true, maintainAspectRatio:true, aspectRatio: 1.5 }
            });
        }

        if(genderCanvas){
            var genderCtx = genderCanvas.getContext('2d');
            genderChart = new Chart(genderCtx, {
                type: 'pie',
                data: {
                    labels: ['男', '女'],
                    datasets: [{
                        data: [0, 0],
                        backgroundColor: ['#007bff', '#e83e8c']
                    }]
                },
                options: { responsive:true, maintainAspectRatio:true, aspectRatio: 1.5 }
            });
        }

        if(deptCanvas){
            var deptCtx = deptCanvas.getContext('2d');
            departmentChart = new Chart(deptCtx, {
                type: 'bar',
                data: {
                    labels: [],
                    datasets: [{
                        label: '借阅次数',
                        data: [],
                        backgroundColor: 'rgba(40,167,69,0.7)',
                        borderRadius: 5
                    }]
                },
                options: { 
                    responsive:true, 
                    maintainAspectRatio:true, 
                    aspectRatio: 1.5,
                    scales: {
                        y: { beginAtZero: true }
                    }
                }
            });
        }
    }

    // 加载统计数据并渲染表格（示例接口）
    function loadStats(){
        $.get('/api/admin/stats').done(function(resp){
            if(!resp || resp.code !== 0 || !resp.data) return;
            var d = resp.data;
            $('#totalBooks').text(d.totalBooks || 0);
            $('#totalReaders').text(d.totalReaders || 0);
            
            var bs = d.borrowStats || {};
            $('#pendingAudit').text(bs.pending || 0);
            $('#unreturned').text(bs.borrowed || 0);
            $('#overdue').text(bs.overdue || 0);
            $('#damagedCount').text(bs.damaged || 0);

            // 更新借阅趋势
            if(borrowTrendChart && d.borrowStats && d.borrowStats.trend){
                var trend = d.borrowStats.trend;
                borrowTrendChart.data.labels = trend.map(function(i){ return i.date; });
                borrowTrendChart.data.datasets[0].data = trend.map(function(i){ return i.count; });
                borrowTrendChart.update();
            }

            // 更新图书分类占比
            if(typePieChart && d.typeStats){
                typePieChart.data.labels = d.typeStats.map(function(i){ return i.name; });
                typePieChart.data.datasets[0].data = d.typeStats.map(function(i){ return i.count; });
                typePieChart.update();
            }

            // 更新性别分布
            if(genderChart && d.genderStats){
                var genderData = [0, 0]; // 男, 女
                d.genderStats.forEach(function(item){
                    if(item.label === 2) genderData[1] = item.count;
                    else genderData[0] += item.count;
                });
                genderChart.data.datasets[0].data = genderData;
                genderChart.update();
            }

            // 更新专业分布
            if(departmentChart && d.departmentStats){
                departmentChart.data.labels = d.departmentStats.map(function(i){ return i.label || '其他'; });
                departmentChart.data.datasets[0].data = d.departmentStats.map(function(i){ return i.count; });
                departmentChart.update();
            }
        }).fail(function(jqxhr, status){
            if(status === 'abort') return;
            console.warn('无法加载统计数据');
        });
    }

    // 初始加载：统一通过片段渲染，避免完整模板与片段风格不一致
    (function initialLoad(){
        loadPageIntoContent('/admin/fragment/dashboard', false);
    })();

    // --- SPA-like navigation: 拦截 .spa-link，AJAX 加载右侧内容并执行内联脚本 ---
    function executeInlineScripts($html){
        $html.find('script').each(function(){
            var src = $(this).attr('src');
            if(src){
                // load external script if not already present
                var exists = $('script[src="'+src+'"]');
                if(exists.length===0){
                    var s = document.createElement('script'); s.src = src; document.body.appendChild(s);
                }
            } else {
                // execute inline
                try{ $.globalEval($(this).text()); }catch(e){ console.error('执行脚本失败', e); }
            }
        });
    }

    function loadPageIntoContent(url, pushState){
        // 将 /admin/... 映射为 /admin/fragment/...，优先加载片段
        var fragmentUrl = url;
        try{
            var u = new URL(url, window.location.origin);
            var p = u.pathname;
            if(p.indexOf('/admin/') === 0 && p.indexOf('/admin/fragment/') !== 0){
                fragmentUrl = p.replace('/admin/', '/admin/fragment/') + u.search;
            }
        }catch(e){
            // 相对路径处理
            if(url.indexOf('/admin/') === 0 && url.indexOf('/admin/fragment/') !== 0){
                fragmentUrl = url.replace('/admin/', '/admin/fragment/');
            }
        }

        $.get(fragmentUrl).done(function(html){
            // 保留 script 元素以便后续执行（keepScripts = true）
            var parsed = $.parseHTML(html, document, true);
            var $frag = $('<div>').append(parsed);
            // 优先注入包含外层 wrapper 的完整片段（保证片段内使用的根选择器可用）
            // 尝试找到最顶层带 id 的元素（例如 #fragmentBooks、#fragmentReaders）并注入其 outerHTML
            var $topChildren = $frag.children();
            var $topWithId = $topChildren.filter('[id]').first();
            if($topWithId && $topWithId.length>0){
                $('.content-inner').html($topWithId.prop('outerHTML'));
            } else {
                // 否则优先注入常见容器的 outerHTML（.content-inner 或 .container），找不到就注入整个 fragment HTML
                var $content = $frag.find('.content-inner').first();
                if(!$content || $content.length===0) $content = $frag.find('.container').first();
                if($content && $content.length>0){
                    $('.content-inner').html($content.prop('outerHTML'));
                } else {
                    console.info('未找到常见容器，使用 fragment 整体内容注入');
                    $('.content-inner').html($frag.html());
                }
            }
            // 执行脚本（外部脚本会被添加一次）
            executeInlineScripts($frag);
            // 页面特定后处理：如果注入的是仪表盘 fragment，则重新初始化图表并拉取数据
            if(fragmentUrl.indexOf('/admin/fragment/dashboard') !== -1 || url.indexOf('/admin/dashboard') !== -1){
                initDashboardCharts();
                // load data after charts ready
                loadStats();
            }
            if(pushState) history.pushState({url:url}, '', url);
        }).fail(function(){
            console.warn('页面加载失败: ' + fragmentUrl + ' (请求原始: ' + url + ')');
        });
    }

    // 点击拦截
    $(document).on('click', 'a.spa-link', function(e){
        var href = $(this).attr('href');
        if(!href || href.indexOf('http')===0 || href.indexOf('mailto:')===0) return; // 放行外链
        e.preventDefault();
        loadPageIntoContent(href, true);
    });

    // 注意：.action-detail 和 .action-return 的事件绑定已移至 fragment_lends.html 中
    // 避免在 admin-dashboard.js 中重复绑定导致冲突

    // 支持后退/前进
    window.addEventListener('popstate', function(e){
        var url = location.pathname + location.search;
        loadPageIntoContent(url, false);
    });

    // 初始加载：进入管理后台时自动加载对应 fragment，避免内容区空白
    var path = window.location.pathname;
    if (path.indexOf('/admin/') === 0) {
        loadPageIntoContent(path, false);
    }
});
