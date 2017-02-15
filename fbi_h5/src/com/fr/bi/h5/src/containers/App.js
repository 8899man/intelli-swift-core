import mixin from 'react-mixin'
import {bindActionCreators} from 'redux';
import {connect} from 'react-redux';
import React, {
    Component,
    StyleSheet,
    PropTypes,
    Portal,
    Text,
    View,
    ListView,
    Fetch,
    Dimensions,
    TouchableBounce,
    TouchableHighlight,
    TouchableOpacity,
    TouchableWithoutFeedback
} from 'lib';

import {ReactComponentWithImmutableRenderMixin, UserAgent} from 'core';
import {Template} from 'data';
import {Layout} from 'layout';
import * as TodoActions from '../actions/template';

import MainContainer4Phone from './phone/vertical/MainContainer.js'
import MainContainer4Pad from './pad/vertical/MainContainer.js'
import MainContainerHorizontal4Phone from './phone/horizontal/MainContainerHorizontal.js'
import MainContainerHorizontal4Pad from './pad/horizontal/MainContainerHorizontal.js'
import MainContainerWeb from './web/MainContainer'

const {width, height} = Dimensions.get('window');

let isMobile = false;
let isPad = false;
if (UserAgent.mobile()) {
    isMobile = true;
    if (UserAgent.ipad()) {
        isPad = true;
    }
    if (UserAgent.android()) {
        var size = window.getComputedStyle(document.body, ':after').getPropertyValue('content');
        if (size.indexOf('smallscreen') != -1) {
            isPad = true;
        }
    }
}

//import PanResponderDemo from '../examples/base/2/PanResponder/PanResponder'
//import ViewDemo from '../examples/base/2/View/View'
//import ScrollViewDemo from '../examples/base/2/ScrollView/ScrollView'
//import ListViewDemo from '../examples/base/2/ListView/ListView';
//import PickerDemo from '../examples/base/2/Picker/Picker'
//import DatePickerIOSDemo from '../examples/base/2/DatePickerIOS/DatePickerIOS'
//import ViewPagerDemo from '../examples/base/2/ViewPager/ViewPager'
//import NavigatorDemo from '../examples/base/2/Navigator/Navigator'
//
//import AutoSizerDemo from '../examples/base/3/AutoSizer/AutoSizer';
//import WheelerDemo from '../examples/base/3/Wheeler/Wheeler';
//import SwiperDemo from '../examples/base/3/Swiper/Swiper';
//
//
//import ScrollSyncDemo from '../examples/base/3/ScrollSync/ScrollSync';
//import CellMeasurerDemo from '../examples/base/3/CellMeasurer/CellMeasurer';
//import ColumnSizerDemo from '../examples/base/3/ColumnSizer/ColumnSizer';
//import GridDemo from '../examples/base/3/Grid/Grid';
//import CollectionDemo from '../examples/base/3/Collection/Collection';
//import VirtualScrollDemo from '../examples/base/3/VirtualScroll/VirtualScroll';
//import InfiniteLoaderDemo from '../examples/base/3/InfiniteLoader/InfiniteLoader';
//import ArrowKeyStepperDemo from '../examples/base/3/ArrowKeyStepper/ArrowKeyStepper';
//import GiftedListViewDemo from '../examples/base/3/GiftedListView/GiftedListView'
//import SideMenu from '../examples/base/3/SideMenu/SideMenu'
//import SortableDemo from '../examples/base/3/Sortable/Sortable'
//import Animatable from '../examples/base/3/Animatable/Animatable'
//
//import TableResizeExample from '../examples/base/3/Table/ResizeExample'
//import TableColumnGroupsExample from '../examples/base/3/Table/ColumnGroupsExample'
//import TableFilterExample from '../examples/base/3/Table/FilterExample'
//import TableFlexGrowExample from '../examples/base/3/Table/FlexGrowExample'
//import TableObjectDataExample from '../examples/base/3/Table/ObjectDataExample'
//import TableSortExample from '../examples/base/3/Table/SortExample'

// import DialogDemo from '../examples/base/3/Dialog/Dialog'

import UIExplorerApp from '../examples/UIExplorer/UIExplorerApp.web'
// import Game2048 from '../examples/2048/Game2048'

// import LayoutDemo from '../examples/base/Layout'


class App extends Component {
    static childContextTypes = {
        actions: PropTypes.object,
        $template: PropTypes.object
    };

    getChildContext() {
        const {actions, $template} = this.props;
        return {
            actions,
            $template
        };
    }

    constructor(props, context) {
        super(props, context);
    }

    state = {
        width,
        height
    };

    componentDidMount() {
        setInterval(() => {
            Fetch(BH.servletURL + '?op=fr_bi_dezi&cmd=update_session', {
                method: "POST",
                body: JSON.stringify({_t: new Date(), sessionID: BH.sessionID})
            });
        }, 30000);
        window.onbeforeunload = ()=> {
            Fetch(BH.servletURL + '?op=closesessionid', {
                method: "POST",
                body: JSON.stringify({_t: new Date(), sessionID: BH.sessionID})
            });
        };
        const resize = ()=> {
            Dimensions.update();
            const {width, height} = Dimensions.get('window')
            this.setState({
                width,
                height
            })
        };
        window.addEventListener("onorientationchange", resize, false);
        window.addEventListener("resize", resize, false);
    }

    render() {
        return <UIExplorerApp />
        // const {width, height} = this.state;
        // let Component = MainContainer4Phone;
        // if (isMobile) {
        //     if (isPad) {
        //         Component = width > height ? MainContainerHorizontal4Pad : MainContainer4Pad;
        //     } else {
        //         Component = width > height ? MainContainerHorizontal4Phone : MainContainer4Phone;
        //     }
        // }
        // if (isMobile) {
        //     return <View>
        //         <Layout flex box='mean'>
        //             <Component width={width} height={height}
        //                        $template={this.props.$template}/>
        //         </Layout>
        //         <Portal />
        //     </View>;
        // }
        //
        // return <View>
        //     <Layout flex box='mean'>
        //         <MainContainerWeb width={width} height={height}
        //                    $template={this.props.$template}/>
        //     </Layout>
        //     <Portal />
        // </View>;
    }

    componentWillUnmount() {
        window.removeEventListener("onorientationchange");
        window.removeEventListener("resize");
    }
}

const styles = StyleSheet.create({
    wrapper: {
        position: 'absolute',
        left: 0,
        right: 0,
        top: 0,
        bottom: 0
    }
});

App.propTypes = {
    actions: PropTypes.object.isRequired,
    $template: PropTypes.object.isRequired
};
mixin.onClass(App, ReactComponentWithImmutableRenderMixin);

function mapStateToProps(state) {
    const props = {
        $template: state.get('template')
    };
    return props;
}

function mapDispatchToProps(dispatch) {
    const actionMap = {actions: bindActionCreators(TodoActions, dispatch)};
    return actionMap;
}

export default
connect(mapStateToProps, mapDispatchToProps)(App);
