import mixin from 'react-mixin'
import ReactDOM from 'react-dom'

import {ReactComponentWithImmutableRenderMixin, requestAnimationFrame, emptyFunction} from 'core'
import React, {
    Component,
    StyleSheet,
    Text,
    Dimensions,
    ListView,
    View,
    Fetch
} from 'lib'

import {Table, AutoSizer} from 'base'

import {Template, Widget} from 'data'

import {MultiSelectorWidget} from 'widgets'


class MultiSelectorComponent extends Component {
    constructor(props, context) {
        super(props, context);
    }

    static propTypes = {};

    static defaultProps = {
        onValueChange: emptyFunction
    };

    state = {};

    componentWillMount() {

    }

    componentDidMount() {

    }

    componentWillReceiveProps() {

    }

    componentWillUpdate() {

    }

    render() {
        const {...props} = this.props;
        const wId = props.wId;
        const template = new Template(props.$template);
        const widget = template.getWidgetById(wId);
        return <MultiSelectorWidget
            style={styles.wrapper}
            type={widget.getSelectType()}
            value={widget.getSelectValue()}
            itemsCreator={(options)=> {
                return widget.getData(options);
            }}
            width={props.width}
            height={props.height}
            onValueChange={(value)=> {
                widget.setWidgetValue(value);
                template.setWidget(wId, widget);
                this.props.onValueChange(template.$get());
            }}
        >
        </MultiSelectorWidget>
    }
}
mixin.onClass(MultiSelectorComponent, ReactComponentWithImmutableRenderMixin);
const styles = StyleSheet.create({
    wrapper: {
        backgroundColor: '#fff'
    }
});
export default MultiSelectorComponent
