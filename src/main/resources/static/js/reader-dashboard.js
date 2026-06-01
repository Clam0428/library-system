/**
 * reader-dashboard.js - 读者端单页跳转及数据加载逻辑
 */
$(function(){
    window.updateUnreadCount = function(){
        $.get('/api/reader/currentUser').done(function(userResp){
            if(!userResp || userResp.code !== 0 || !userResp.data) return;
            var readerId = userResp.data.id;
            $.get('/api/message/unread/count/' + readerId).done(function(resp){
                if(resp && resp.code === 0){
                    var count = resp.data || 0;
                    if(count > 0){
                        $('#unreadBadge').text(count).show();
                    } else {
                        $('#unreadBadge').hide();
                    }
                }
            });
        });
    };

    // 2. 登出逻辑（取消弹窗，点击即登出）
    $('#logoutBtn').click(function(){
        $.post('/api/reader/logout').always(function(){
            window.location.href = '/login';
        });
    });

    // 4. SPA-like navigation
    function executeInlineScripts($html){
        $html.find('script').each(function(){
            var src = $(this).attr('src');
            if(src){
                var exists = $('script[src="'+src+'"]');
                if(exists.length===0){
                    var s = document.createElement('script'); s.src = src; document.body.appendChild(s);
                }
            } else {
                try{ $.globalEval($(this).text()); }catch(e){ console.error('执行内联脚本失败', e); }
            }
        });
    }

    // --- 全局头像上传逻辑 ---
    // 移除导致 RangeError 的手动 .click() 触发逻辑，改用 label + input 原生绑定
    
    $(document).on('change', '#avatarInput', function(e) {
        var file = this.files[0];
        var $avatarImg = $('#userAvatarImg');
        var $defaultIcon = $('#defaultAvatarIcon');
        
        if (!file) {
            return;
        }
        
        // 验证文件类型
        if (!file.type.match(/^image\/(jpeg|jpg|png|gif|webp)$/i)) {
            if(window.ui && window.ui.toast) window.ui.toast('请选择有效的图片文件（JPG、PNG、GIF、WebP）', 'warning');
            return;
        }
        
        // 验证文件大小（最大 5MB）
        if (file.size > 5 * 1024 * 1024) {
            if(window.ui && window.ui.toast) window.ui.toast('图片大小不能超过 5MB', 'warning');
            return;
        }
        
        var formData = new FormData();
        formData.append('file', file);

        $.ajax({
            url: '/api/reader/uploadAvatar',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            success: function(resp) {
                if (resp && resp.code === 0) {
                    var avatarUrl = resp.data;

                    function doUpdateAvatar(readerId) {
                        if (!readerId) {
                            if(window.ui && window.ui.toast) window.ui.toast('无法获取当前用户信息，暂时不能更换头像', 'danger');
                            return;
                        }
                        $.ajax({
                            url: '/api/reader/update',
                            type: 'POST',
                            contentType: 'application/json',
                            data: JSON.stringify({
                                id: readerId,
                                avatar: avatarUrl
                            }),
                            success: function(updateResp) {
                                if (updateResp && updateResp.code === 0) {
                                    $avatarImg.attr('src', avatarUrl + '?t=' + new Date().getTime()).show();
                                    $defaultIcon.hide();
                                    // 关键修复：更换头像后，重新执行当前页面的 loadProfile 以同步内存中的数据
                                    if (typeof window.reloadCurrentProfile === 'function') {
                                        window.reloadCurrentProfile();
                                    }
                                    if(window.ui && window.ui.toast) window.ui.toast('头像更换成功', 'success');
                                } else {
                                    if(window.ui && window.ui.toast) window.ui.toast('保存头像路径失败: ' + ((updateResp.message || updateResp.msg) || '未知错误'), 'danger');
                                }
                            },
                            error: function() {
                                if(window.ui && window.ui.toast) window.ui.toast('保存头像路径请求失败', 'danger');
                            }
                        });
                    }

                    // 优先从当前页面隐藏域获取 readerId，不存在时再从后端获取
                    var readerId = $('#readerId').val() || $('input[name="id"]').val();
                    if (readerId) {
                        doUpdateAvatar(readerId);
                    } else {
                        $.get('/api/reader/currentUser').done(function(userResp){
                            if (userResp && userResp.code === 0 && userResp.data && userResp.data.id) {
                                doUpdateAvatar(userResp.data.id);
                            } else {
                                if(window.ui && window.ui.toast) window.ui.toast('无法获取当前用户信息，暂时不能更换头像', 'danger');
                            }
                        }).fail(function(){
                            if(window.ui && window.ui.toast) window.ui.toast('无法获取当前用户信息，暂时不能更换头像', 'danger');
                        });
                    }
                } else {
                    if(window.ui && window.ui.toast) window.ui.toast('上传失败: ' + ((resp.message || resp.msg) || '未知错误'), 'danger');
                }
            },
            error: function(xhr, status, error) {
                if(window.ui && window.ui.toast) window.ui.toast('服务器异常，请稍后再试: ' + (error || ''), 'danger');
            }
        });
    });
    // --- 全局头像上传逻辑结束 ---

    window.loadPageIntoContent = function(url, pushState){
        var fragmentUrl = url;
        if(url.indexOf('/reader/') === 0 && url.indexOf('/reader/fragment/') !== 0){
            fragmentUrl = url.replace('/reader/', '/reader/fragment/');
        }
        
        // 仪表盘特殊处理：/reader/dashboard 对应 /reader/fragment/dashboard
        if(url === '/reader/dashboard') fragmentUrl = '/reader/fragment/dashboard';

        $.get(fragmentUrl).done(function(html){
            var parsed = $.parseHTML(html, document, true);
            var $frag = $('<div>').append(parsed);
            
            // 查找片段中的主容器（如 #fragmentReaderDashboard）
            var $topWithId = $frag.children().filter('[id]').first();
            if($topWithId.length > 0){
                $('.content-inner').html($topWithId.prop('outerHTML'));
            } else {
                $('.content-inner').html($frag.html());
            }

            executeInlineScripts($frag);
            
            // 更新 Sidebar 状态
            $('.sidebar .nav-link').removeClass('active');
            $('.sidebar .nav-link[href="'+url+'"]').addClass('active');

            if(pushState) history.pushState({url:url}, '', url);
        }).fail(function(){
            console.error('加载页面失败: ' + fragmentUrl);
        });
    }

    // 拦截 spa-link 点击
    $(document).on('click', 'a.spa-link', function(e){
        var href = $(this).attr('href');
        if(!href || href.startsWith('http')) return;
        e.preventDefault();
        loadPageIntoContent(href, true);
    });

    // 初始加载：根据当前路径加载，默认进入图书查询（与侧边栏首项一致）
    var path = window.location.pathname;
    if (path.startsWith('/reader/') && path !== '/reader/dashboard') {
        loadPageIntoContent(path, false);
    } else {
        loadPageIntoContent('/reader/books', false);
    }
    updateUnreadCount();

    // 浏览器后退支持
    window.addEventListener('popstate', function(e){
        var url = location.pathname;
        if(url.startsWith('/reader/')) loadPageIntoContent(url, false);
    });
});
